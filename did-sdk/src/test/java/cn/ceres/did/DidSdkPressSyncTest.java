package cn.ceres.did;

import cn.ceres.did.client.SdkClient;
import cn.ceres.did.sdk.SdkProto;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 同步请求压测
 *
 * @author ehlxr
 */
public class DidSdkPressSyncTest {
    private static final Logger logger = LoggerFactory.getLogger(DidSdkPressSyncTest.class);
    private static int NUM = 60000;

    @Test
    public void pressSyncTest() throws Exception {
        SdkClient client = new SdkClient("10.19.248.200",30581);
        client.init();
        client.start();

        long start;
        long end;
        long cast;
        long amount = 0;
        long allcast = 0;

        for (int k = 0; k < 20; k++) {
            start = System.currentTimeMillis();
            for (int i = 0; i < NUM; i++) {
                final SdkProto sdkProto = new SdkProto();
                client.invokeSync(sdkProto, 5000);
            }

            end = System.currentTimeMillis();
            cast = (end - start);
            allcast += cast;

            logger.info("invokeSync test num is: {}, cast time: {} millsec, throughput: {} send/millsec", NUM, cast, (double) NUM / cast);

            amount += NUM;
            // NUM += 5000;
            // TimeUnit.SECONDS.sleep(2);
        }

        logger.info("invokeSync test all num is: {}, all cast time: {} millsec, all throughput: {} send/millsec", amount, allcast, (double) amount / allcast);
    }
}
