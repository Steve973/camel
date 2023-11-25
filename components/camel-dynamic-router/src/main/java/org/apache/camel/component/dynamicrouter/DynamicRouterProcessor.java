package org.apache.camel.component.dynamicrouter;

import org.apache.camel.AsyncCallback;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Route;
import org.apache.camel.impl.engine.DefaultRoute;
import org.apache.camel.model.RecipientListDefinition;
import org.apache.camel.processor.RecipientList;
import org.apache.camel.reifier.RecipientListReifier;
import org.apache.camel.support.AsyncProcessorSupport;

import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.apache.camel.component.dynamicrouter.DynamicRouterConstants.RECIPIENT_LIST_EXPRESSION;
import static org.apache.camel.component.dynamicrouter.DynamicRouterConstants.RECIPIENT_LIST_HEADER;

public class DynamicRouterProcessor extends AsyncProcessorSupport {

    /**
     * Template for a logging endpoint, showing all, and multiline.
     */
    private static final String LOG_ENDPOINT = "log:%s.%s?level=%s&showAll=true&multiline=true";

    private final TreeSet<PrioritizedFilter> filters;

    private final RecipientList recipientList;

    private final boolean warnDroppedMessage;

    private final String channel;

    /**
     * Indicates the behavior of the Dynamic Router when routing participants are selected to receive an incoming
     * exchange. If the mode is "firstMatch", then the exchange is routed only to the first participant that has a
     * matching predicate. If the mode is "allMatch", then the exchange is routed to all participants that have a
     * matching predicate.
     */
    private final String recipientMode;

    public DynamicRouterProcessor(TreeSet<PrioritizedFilter> filters, RecipientList recipientList,
                                  String recipientMode, boolean warnDroppedMessage, String channel) {
        this.filters = filters;
        this.recipientList = recipientList;
        this.recipientMode = recipientMode;
        this.warnDroppedMessage = warnDroppedMessage;
        this.channel = channel;
    }

    /**
     * Match the exchange against all {@link #filters} to determine if any of them are suitable to handle the
     * exchange.
     *
     * @param exchange the message exchange
     * @return list of filters that match for the exchange; if "firstMatch" mode, it is a singleton list of
     * that filter
     */
    protected List<PrioritizedFilter> matchFilters(final Exchange exchange) {
        return filters.stream()
                .filter(f -> f.predicate().matches(exchange))
                .limit("allMatch".equals(recipientMode) ? Integer.MAX_VALUE : 1)
                .collect(Collectors.toList());
    }

    public void prepareExchange(Exchange exchange) {
        Message message = exchange.getMessage();
        List<PrioritizedFilter> matchedFilters = matchFilters(exchange);
        String recipients = matchedFilters.stream()
                .map(PrioritizedFilter::endpoint)
                .collect(Collectors.joining(","));
        if (recipients.isEmpty()) {
            message.setHeader("originalBody", message.getBody());
            recipients = String.format(LOG_ENDPOINT, this.getClass().getCanonicalName(), channel,
                    warnDroppedMessage ? "WARN" : "DEBUG");
            String error = String.format(
                    "DynamicRouter channel '%s': no filters matched for an exchange from route: '%s'.  " +
                            "The 'originalBody' header contains the original message body.",
                    channel, exchange.getFromEndpoint());
            message.setBody(error, String.class);
        }
        message.setHeader(RECIPIENT_LIST_HEADER, recipients);
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        prepareExchange(exchange);
        recipientList.process(exchange);
    }

    /**
     * Process the exchange, and use the {@link AsyncCallback} to signal completion.
     *
     * @param  exchange the exchange to process
     * @param  callback the {@link AsyncCallback} to signal when asynchronous processing has completed
     * @return          true to continue to execute synchronously, or false to continue to execute asynchronously
     */
    @Override
    public boolean process(final Exchange exchange, final AsyncCallback callback) {
        prepareExchange(exchange);
        return recipientList.process(exchange, callback);
    }

    public static class DynamicRouterProcessorFactory {

        public DynamicRouterProcessor getInstance(CamelContext camelContext, DynamicRouterConfiguration configuration,
                                                  TreeSet<PrioritizedFilter> filters) throws Exception {
            RecipientListDefinition<?> definition = new RecipientListDefinition<>(RECIPIENT_LIST_EXPRESSION)
                    .parallelProcessing(configuration.isParallelProcessing())
                    .synchronous(configuration.isSynchronous())
                    .parallelAggregate(configuration.isParallelAggregate())
                    .delimiter(",")
                    .aggregationStrategy(configuration.getAggregationStrategy())
                    .cacheSize(configuration.getCacheSize())
                    .onPrepare(configuration.getOnPrepare())
                    .timeout(configuration.getTimeout())
                    .executorService(configuration.getExecutorService());
            if (configuration.isStreaming()) {
                definition.streaming();
            }
            if (configuration.isShareUnitOfWork()) {
                definition.shareUnitOfWork();
            }
            if (configuration.isStopOnException()) {
                definition.stopOnException();
            }
            if (configuration.isIgnoreInvalidEndpoints()) {
                definition.ignoreInvalidEndpoints();
            }
            Route dummyRoute = new DefaultRoute(camelContext, null, "", "", null, null);
            RecipientListReifier reifier = new RecipientListReifier(dummyRoute, definition);
            return new DynamicRouterProcessor(filters, (RecipientList) reifier.createProcessor(),
                    configuration.getRecipientMode(), configuration.isWarnDroppedMessage(), configuration.getChannel());
        }
    }
}