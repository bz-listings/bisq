/*
 * This file is part of Bisq.
 *
 * Bisq is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bisq is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bisq. If not, see <http://www.gnu.org/licenses/>.
 */

package bisq.core.btc;

import bisq.core.app.AppOptionKeys;
import bisq.core.app.BisqEnvironment;
import bisq.core.btc.model.AddressEntryList;
import bisq.core.btc.nodes.BtcNodes;
import bisq.core.btc.setup.RegTestHost;
import bisq.core.btc.setup.WalletsSetup;
import bisq.core.btc.wallet.BsqCoinSelector;
import bisq.core.btc.wallet.BsqWalletService;
import bisq.core.btc.wallet.BtcWalletService;
import bisq.core.btc.wallet.NonBsqCoinSelector;
import bisq.core.btc.wallet.TradeWalletService;
import bisq.core.provider.PriceNodeHttpClient;
import bisq.core.provider.ProvidersRepository;
import bisq.core.provider.fee.FeeProvider;
import bisq.core.provider.fee.FeeService;
import bisq.core.provider.price.PriceFeedService;

import bisq.common.app.AppModule;

import org.springframework.core.env.Environment;

import com.google.inject.Singleton;
import com.google.inject.name.Names;

import java.io.File;

import java.util.Arrays;

import static com.google.inject.name.Names.named;

public class BitcoinModule extends AppModule {
    public BitcoinModule(Environment environment) {
        super(environment);
    }

    @Override
    protected void configure() {
        // If we have selected BTC_DAO_REGTEST or BTC_DAO_TESTNET we use our master regtest node,
        // otherwise the specified host or default (localhost)
        String regTestHost = environment.getProperty(BtcOptionKeys.REG_TEST_HOST, String.class, "");
        if (regTestHost.isEmpty()) {
            regTestHost = BisqEnvironment.getBaseCurrencyNetwork().isDaoTestNet() ?
                    "104.248.31.39" :
                    BisqEnvironment.getBaseCurrencyNetwork().isDaoRegTest() ?
                            "134.209.242.206" :
                            RegTestHost.DEFAULT_HOST;
        }

        RegTestHost.HOST = regTestHost;
        if (Arrays.asList("localhost", "127.0.0.1").contains(regTestHost)) {
            bind(RegTestHost.class).toInstance(RegTestHost.LOCALHOST);
        } else if ("none".equals(regTestHost)) {
            bind(RegTestHost.class).toInstance(RegTestHost.NONE);
        } else {
            bind(RegTestHost.class).toInstance(RegTestHost.REMOTE_HOST);
        }

        bindConstant().annotatedWith(named(UserAgent.NAME_KEY)).to(environment.getRequiredProperty(UserAgent.NAME_KEY));
        bindConstant().annotatedWith(named(UserAgent.VERSION_KEY)).to(environment.getRequiredProperty(UserAgent.VERSION_KEY));
        bind(UserAgent.class).in(Singleton.class);

        File walletDir = new File(environment.getRequiredProperty(BtcOptionKeys.WALLET_DIR));
        bind(File.class).annotatedWith(named(BtcOptionKeys.WALLET_DIR)).toInstance(walletDir);

        bindConstant().annotatedWith(named(BtcOptionKeys.BTC_NODES)).to(environment.getRequiredProperty(BtcOptionKeys.BTC_NODES));
        bindConstant().annotatedWith(named(BtcOptionKeys.USER_AGENT)).to(environment.getRequiredProperty(BtcOptionKeys.USER_AGENT));
        bindConstant().annotatedWith(named(BtcOptionKeys.NUM_CONNECTIONS_FOR_BTC)).to(environment.getRequiredProperty(BtcOptionKeys.NUM_CONNECTIONS_FOR_BTC));
        bindConstant().annotatedWith(named(BtcOptionKeys.USE_ALL_PROVIDED_NODES)).to(environment.getRequiredProperty(BtcOptionKeys.USE_ALL_PROVIDED_NODES));
        bindConstant().annotatedWith(named(BtcOptionKeys.USE_TOR_FOR_BTC)).to(environment.getRequiredProperty(BtcOptionKeys.USE_TOR_FOR_BTC));
        bindConstant().annotatedWith(named(BtcOptionKeys.IGNORE_LOCAL_BTC_NODE)).to(environment.getRequiredProperty(BtcOptionKeys.IGNORE_LOCAL_BTC_NODE));
        String socks5DiscoverMode = environment.getProperty(BtcOptionKeys.SOCKS5_DISCOVER_MODE, String.class, "ALL");
        bind(String.class).annotatedWith(Names.named(BtcOptionKeys.SOCKS5_DISCOVER_MODE)).toInstance(socks5DiscoverMode);
        bindConstant().annotatedWith(named(AppOptionKeys.PROVIDERS)).to(environment.getRequiredProperty(AppOptionKeys.PROVIDERS));

        bind(AddressEntryList.class).in(Singleton.class);
        bind(WalletsSetup.class).in(Singleton.class);
        bind(BtcWalletService.class).in(Singleton.class);
        bind(BsqWalletService.class).in(Singleton.class);
        bind(TradeWalletService.class).in(Singleton.class);
        bind(BsqCoinSelector.class).in(Singleton.class);
        bind(NonBsqCoinSelector.class).in(Singleton.class);
        bind(BtcNodes.class).in(Singleton.class);
        bind(Balances.class).in(Singleton.class);

        bind(PriceNodeHttpClient.class).in(Singleton.class);

        bind(ProvidersRepository.class).in(Singleton.class);
        bind(FeeProvider.class).in(Singleton.class);
        bind(PriceFeedService.class).in(Singleton.class);
        bind(FeeService.class).in(Singleton.class);
        bind(TxFeeEstimationService.class).in(Singleton.class);
    }
}

