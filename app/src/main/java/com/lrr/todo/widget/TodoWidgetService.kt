package com.lrr.todo.widget

import android.content.Intent
import android.widget.RemoteViewsService

class TodoWidgetService : RemoteViewsService() {

        override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
            return TodoRemoteViewsFactory(this.applicationContext, intent)
        }
    }