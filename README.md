# GrabSomeFood-Test

<img align="center" src="https://lh4.ggpht.com/9BHS5-tq6GtrxYpiHXwETGoeLY3ojy4zK6cey76koixcOGV_7yCJnmLpwEVwY5odqtQ" width="180px" height="300px" />
<img align="center" src="https://lh4.ggpht.com/OucAVVLLLv-yDU2JJfRk6wA7Ous1cwuc4YpfzcEyMypyKze4pcXScrSngdgBIqHnCA" width="180px" height="300px" />
<img align="center" src="https://lh5.ggpht.com/I9LQo-LMh1MMvhYIz4uopvsYUsB2_h6eUCO-QDJl4SGPA7uAjV9pV5y7myMDBG5ni8o" width="180px" height="300px" />

GrabSomeFood Test is a proof of concept Android app for FOODit, the new platform to build super simple technology to get your restaurant on-line and generating new orders in 5 days.

## Download ready to use app

[![App Icon](https://developer.android.com/images/brand/en_generic_rgb_wo_60.png)](https://play.google.com/store/apps/details?id=com.foodittest)

The app is released on Google Play: https://play.google.com/store/apps/details?id=com.foodittest.

## Details

- GrabSomeFood Test shows a list of cards with the available meals a restaurant offers. Every time some plate is added to the order the order is saved on a SQLite database on the Android device so that the orders are back when the app is restarted. On click, the description of every meal is expandable/collapsible.
- The top bar can be expanded and the order in place will be shown. If there are too many meals to be shown on screen at once the list can be scrolled down to reach all the items on the order.
- The app also saves the restaurant menu on a SQLite database so that in case there is no internet the cached version of the menu will be used.
- The menu data is retrieved from http://demo3159884.mockable.io/. Any change done on that JSON feed will be instantly reflected on the app after refreshing it.
- To refresh the menu you just have to swipe down at the top of the menu list. A spinning arrow will be shown untill the load is completed.
- GrabSomeFood Test works seamlessly in both portrait and landscape modes. The screen can be rotated even when there is some data load in progress. If the red top bar is extended it will keep extended after rotation.
- GrabSomeFood Test can run on devices with Android 4.0+. On Android 4.4+ the status bar and the navigation bar are transparent. On Android 5.0+ the Material theme is used.
- To implement the menu list the new Android 5.0 CardView widget has been used (http://developer.android.com/training/material/lists-cards.html#CardView). The widget and the app will work as long as your device is Android 4.0+.

You can check the mock-ups used for the design of the app on https://github.com/FOODit/FOODit-MobileTest.

For more information visit FOODit on Google Play (https://play.google.com/store/apps/developer?id=FOODit) or visit the website http://foodit.com.
