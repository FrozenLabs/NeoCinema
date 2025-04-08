package com.neocinema.fabric.cef;

import com.neocinema.fabric.NeoCinema;
import com.neocinema.fabric.NeoCinemaClient;
import com.neocinema.fabric.screen.Screen;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.CefLoadHandler;
import org.cef.network.CefRequest;

public class CefBrowserCinemaLoadHandler implements CefLoadHandler {

    @Override
    public void onLoadingStateChange(CefBrowser browser, boolean isLoading, boolean canGoBack, boolean canGoForward) {
    }

    @Override
    public void onLoadStart(CefBrowser browser, CefFrame frame, CefRequest.TransitionType transitionType) {
    }

    @Override
    public void onLoadEnd(CefBrowser browser, CefFrame frame, int httpStatusCode) {
        Screen screen = NeoCinemaClient.getInstance().getScreenManager().getScreen(browser.getIdentifier());

        if (screen == null) {
            return;
        }

        screen.startVideo();

        if (screen.isMuted()) {
            screen.setVideoVolume(0);
        } else {
            screen.setVideoVolume(NeoCinemaClient.getInstance().getVideoSettings().getVolume());
        }
    }

    @Override
    public void onLoadError(CefBrowser browser, CefFrame frame, ErrorCode errorCode, String errorText, String failedUrl) {
        NeoCinema.LOGGER.warning("Load error: " + errorText);
    }

}
