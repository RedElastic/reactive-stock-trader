import com.google.inject.AbstractModule;
import com.lightbend.lagom.javadsl.server.ServiceGuiceSupport;
import com.redelastic.stockbroker.wireTransfer.impl.WireTransferServiceImpl;
import com.redelastic.stockbroker.wireTransfer.impl.transfer.TransferRepository;
import com.redelastic.stockbroker.wireTransfer.impl.transfer.TransferRepositoryImpl;
import com.redelastic.stocktrader.portfolio.api.PortfolioService;
import com.redelastic.stocktrader.wiretransfer.api.WireTransferService;

@SuppressWarnings("WeakerAccess")
public class Module extends AbstractModule implements ServiceGuiceSupport {
    @Override
    protected void configure() {
        bindService(WireTransferService.class, WireTransferServiceImpl.class);
        bind(TransferRepository.class).to(TransferRepositoryImpl.class);
        bindClient(PortfolioService.class);
    }
}
