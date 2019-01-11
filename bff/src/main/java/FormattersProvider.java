import com.redelastic.stocktrader.order.OrderType;
import play.data.format.Formatters;
import play.data.format.Formatters.SimpleFormatter;
import play.i18n.MessagesApi;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.text.ParseException;
import java.util.Locale;


@Singleton
class FormattersProvider implements Provider<Formatters> {

    private final MessagesApi messagesApi;

    @Inject
    public FormattersProvider(MessagesApi messagesApi) {
        this.messagesApi = messagesApi;
    }

    @Override
    public Formatters get() {
        Formatters formatters = new Formatters(messagesApi);

        formatters.register(OrderType.class, orderTypeFormatter);

        return formatters;
    }

    private SimpleFormatter<OrderType> orderTypeFormatter = new SimpleFormatter<OrderType>() {
        @Override
        public OrderType parse(String text, Locale locale) throws ParseException {
            try {
                return OrderType.valueOf(text.toUpperCase());
            } catch (Exception ex) {
                throw new ParseException(
                        String.format("Unable to parse OrderType from %s", text), 0);
            }
        }

        @Override
        public String print(OrderType orderType, Locale locale) {
            return orderType.name();
        }
    };
}