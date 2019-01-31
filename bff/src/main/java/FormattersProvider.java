import com.redelastic.stocktrader.TradeType;
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

        formatters.register(TradeType.class, orderTypeFormatter);

        return formatters;
    }

    private final SimpleFormatter<TradeType> orderTypeFormatter = new SimpleFormatter<TradeType>() {
        @Override
        public TradeType parse(String text, Locale locale) throws ParseException {
            try {
                return TradeType.valueOf(text.toUpperCase());
            } catch (Exception ex) {
                throw new ParseException(
                        String.format("Unable to parse TradeType from %s", text), 0);
            }
        }

        @Override
        public String print(TradeType tradeType, Locale locale) {
            return tradeType.name();
        }
    };
}