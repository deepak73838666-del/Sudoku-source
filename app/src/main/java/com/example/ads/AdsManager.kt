package com.example.ads

import android.content.Context

interface AdsManager {
    fun loadBanner(context: Context)
    // Compose View for Banner
    @androidx.compose.runtime.Composable
    fun BannerAd()
    
    fun loadInterstitial(context: Context)
    fun showInterstitial(context: Context, onAdDismissed: () -> Unit)
    
    fun loadRewarded(context: Context)
    fun showRewarded(context: Context, onRewardEarned: () -> Unit, onAdDismissed: () -> Unit)
}

class FakeAdsManager : AdsManager {
    override fun loadBanner(context: Context) {}
    
    @androidx.compose.runtime.Composable
    override fun BannerAd() {
        // Placeholder for real banner
    }
    
    override fun loadInterstitial(context: Context) {}
    
    override fun showInterstitial(context: Context, onAdDismissed: () -> Unit) {
        onAdDismissed()
    }
    
    override fun loadRewarded(context: Context) {}
    
    override fun showRewarded(context: Context, onRewardEarned: () -> Unit, onAdDismissed: () -> Unit) {
        onRewardEarned()
        onAdDismissed()
    }
}
