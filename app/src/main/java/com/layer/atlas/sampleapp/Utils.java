package com.layer.atlas.sampleapp;

import com.layer.atlas.sampleapp.provider.LoggedInContact;
import com.layer.atlas.sampleapp.provider.Provider;
import com.layer.atlas.sampleapp.provider.providers.RailsProvider;
import com.layer.sdk.LayerClient;
import com.layer.sdk.exceptions.LayerException;
import com.layer.sdk.listeners.LayerAuthenticationListener;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

public class Utils {
    public final static String APP_ID = "9ec30af8-5591-11e4-af9e-f7a201004a3b";
    public final static String GCM_SENDER_ID = "565052870572";
    public final static String PROVIDER_URL = "http://layer-identity-provider.herokuapp.com";
    public final static String EMAIL = "dvorak10@layer.com";
    public final static String PASSWORD = "password";

    public final static String EXTRA_PARTICIPANTS = "participants";
    public final static String EXTRA_CONVERSATION_ID = "conversationId";

    private static RailsProvider sProvider;
    private static LayerClient sLayerClient;
    private static LayerAuthenticationListener sChallengeResponder;

    public static interface Callback {
        public void onSuccess();

        public void onError();
    }

    public static RailsProvider getProvider() {
        if (sProvider == null) {
            sProvider = new RailsProvider(APP_ID, PROVIDER_URL);
        }
        return sProvider;
    }

    public static void setLayerClient(LayerClient layerClient) {
        if (sLayerClient == null) {
            sLayerClient = layerClient;
            sLayerClient.registerAuthenticationListener(getChallengeResponder());
        }
    }

    public static LayerAuthenticationListener getChallengeResponder() {
        if (sChallengeResponder == null) {
            sChallengeResponder = new LayerAuthenticationListener() {
                @Override
                public void onAuthenticated(LayerClient client, String s) {

                }

                @Override
                public void onDeauthenticated(LayerClient client) {

                }

                @Override
                public void onAuthenticationChallenge(LayerClient client, final String nonce) {
                    Runnable r = new Runnable() {
                        @Override
                        public void run() {
                            LoggedInContact login = onChallenge(nonce);
                            sLayerClient.answerAuthenticationChallenge(login.getIdentityToken());
                        }
                    };
                    new Thread(r).start();
                }

                @Override
                public void onAuthenticationError(LayerClient client, LayerException e) {

                }
            };
        }
        return sChallengeResponder;
    }

    public static LoggedInContact onChallenge(String nonce) {
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicReference<LoggedInContact> login = new AtomicReference<LoggedInContact>(null);
        getProvider().providerLogin(EMAIL, PASSWORD, nonce, new Provider.LoginCallback() {
            @Override
            public void onLoginSuccess(LoggedInContact loggedInContact) {
                login.set(loggedInContact);
                latch.countDown();
            }

            @Override
            public void onLoginError(String error) {
                throw new IllegalArgumentException(error);
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return login.get();
    }

}
