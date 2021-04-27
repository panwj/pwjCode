package com.example.keepalive.moudle.keepalive

class Constants {

    companion object {
        //    Extra end
        public const val FIVE_MINUTE = 5 * 60 * 1000.toLong()
        public const val FIFTEEN_MINUTE = 15 * 60 * 1000.toLong()
        public const val ONE_MINUTE = 60 * 1000.toLong()
        public const val ONE_HOUR = 60 * 60 * 1000.toLong()
        public const val ONE_DAY = 24 * 60 * 60 * 1000.toLong()

        public const val NOTIFICATION_SHORTCUT_CHANNEL : String = "feature shortcut"
        public const val NOTIFICATION_APP_CHANNEL_ID = "keepalive"
        public const val NOTIFICATION_APP_CHANNEL = "keepalive"
    }
}