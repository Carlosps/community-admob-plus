package admob.plus.cordova.ads;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewarded.ServerSideVerificationOptions;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd;
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback;

import admob.plus.cordova.ExecuteContext;
import admob.plus.cordova.Generated.Events;
import admob.plus.core.Context;

public class RewardedInterstitial extends AdBase {
    private RewardedInterstitialAd mAd = null;

    public RewardedInterstitial(ExecuteContext ctx) {
        super(ctx);
    }

    @Override
    public void onDestroy() {
        clear();
        super.onDestroy();
    }

    @Override
    public void load(Context ctx) {
        clear();

        RewardedInterstitialAd.load(getActivity(), adUnitId, adRequest, new RewardedInterstitialAdLoadCallback() {
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                mAd = null;
                emit(Events.AD_LOAD_FAIL, loadAdError);
                ctx.reject(loadAdError.getCode() + ": " + loadAdError.getMessage());
            }

            @Override
            public void onAdLoaded(@NonNull RewardedInterstitialAd rewardedAd) {
                mAd = rewardedAd;

                ServerSideVerificationOptions ssv = ctx.optServerSideVerificationOptions();
                if (ssv != null) {
                    mAd.setServerSideVerificationOptions(ssv);
                }

                mAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                    @Override
                    public void onAdDismissedFullScreenContent() {
                        emit(Events.AD_DISMISS);
                    }

                    @Override
                    public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
                        emit(Events.AD_SHOW_FAIL, adError);
                    }

                    @Override
                    public void onAdShowedFullScreenContent() {
                        clear();
                        emit(Events.AD_SHOW);
                    }

                    @Override
                    public void onAdImpression() {
                        emit(Events.AD_IMPRESSION);
                    }
                });

                emit(Events.AD_LOAD);
                ctx.resolve();
            }
        });
    }

    @Override
    public boolean isLoaded() {
        return mAd != null;
    }

    @Override
    public void show(Context ctx) {
        if (isLoaded()) {
            mAd.show(getActivity(), rewardItem -> emit(Events.AD_REWARD, rewardItem));
            ctx.resolve();
        } else {
            ctx.reject("Ad is not loaded");
        }
    }

    private void clear() {
        if (mAd != null) {
            mAd.setFullScreenContentCallback(null); // Prevent memory leaks
            mAd = null;
        }
    }
}
