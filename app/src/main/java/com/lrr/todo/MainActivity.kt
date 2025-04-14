package com.lrr.todo

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.View.GONE
import android.view.View.INVISIBLE
import android.view.View.OnClickListener
import android.view.View.VISIBLE
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.loper7.date_time_picker.DateTimeConfig
import com.lrr.todo.adapter.GroupNameListAdapter
import com.lrr.todo.adapter.TodoListAdapter
import com.lrr.todo.data.models.GroupListBean
import com.lrr.todo.data.models.ToDoData
import com.lrr.todo.databinding.ActivityMainBinding
import com.lrr.todo.listener.TodoListChangeListener
import com.lrr.todo.util.AlarmUtils
import com.lrr.todo.util.DateUtils
import com.lrr.todo.util.NotificationUtils
import com.lrr.todo.util.ScreenUtils
import com.lrr.todo.util.SharedPreferenceUtils
import com.lrr.todo.viewmodel.TodoViewModel
import com.lrr.todo.widget.TodoWidgetProvider
import com.lrr.todo.widget.WarningDialogFragment

class MainActivity : AppCompatActivity(), OnClickListener,
    GroupNameListAdapter.GroupNameListChangeListener, TodoListChangeListener {

    companion object {
        const val TAG = "MainActivity"
        const val EXTRA_CURRENT_GROUP_NAME = "currentGroupName "
        const val EXTRA_WIDGET_ID = "widgetId"
        const val EXTRA_CREATE_NEW_GROUP = "createNewGroup"
        const val EXTRA_WIDGET_SIZE = "widgetSize"
        const val WIDGET_SIZE_SMALL = 0
        const val WIDGET_SIZE_MIDDLE = 1
        const val WIDGET_SIZE_LARGE = 2
    }

    private val todoViewModel: TodoViewModel by viewModels()
    private lateinit var mBinding: ActivityMainBinding
    private val groupNameAdapter: GroupNameListAdapter by lazy { GroupNameListAdapter(this) }
    private val todoListAdapter: TodoListAdapter by lazy { TodoListAdapter(this) }
    private var groupList: MutableList<GroupListBean> = ArrayList()
    private var todoList: MutableList<ToDoData> = ArrayList()
    private var todoMap: MutableMap<String, MutableList<ToDoData>> = mutableMapOf()
    private lateinit var imm: InputMethodManager
    private var selectedGroup = ""
    private var widgetId: Int? = null
    private var widgetSize: Int = WIDGET_SIZE_SMALL
    private var currentTodoData: ToDoData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        initViews()
        initData()
    }

    private fun initViews() {
        initGroupNameRv()
        initTodoListRv()
        initCreateTodoEditText()
        initDateTimePicker()
    }

    private fun initDateTimePicker() {
        mBinding.picker.setDisplayType(
            intArrayOf(
                DateTimeConfig.YEAR,
                DateTimeConfig.MONTH,
                DateTimeConfig.DAY,
                DateTimeConfig.HOUR,
                DateTimeConfig.MIN
            )
        )
        mBinding.picker.setLabelText(
            year = getString(R.string.year),
            month = getString(R.string.month),
            day = getString(R.string.day),
            hour = getString(R.string.hour),
            min = getString(R.string.minute)
        )
        mBinding.picker.setMinMillisecond(System.currentTimeMillis())
    }

    private fun initData() {
        if (intent != null) {
            widgetId = intent.getIntExtra(EXTRA_WIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            widgetSize = intent.getIntExtra(EXTRA_WIDGET_SIZE, WIDGET_SIZE_SMALL)
            if (intent.getBooleanExtra(EXTRA_CREATE_NEW_GROUP, false)) {
                createNewGroup()
            } else {
                val groupName = intent.getStringExtra(EXTRA_CURRENT_GROUP_NAME)
                if (!groupName.isNullOrEmpty()) {
                    changeSelectedGroup(groupName)
                }
            }
        }

        todoViewModel.getAllData.observe(this) { dataList ->
            // 更新Map---包含所有数据
            todoMap.clear()
            dataList.forEach {
                if (todoMap.containsKey(it.groupName)) {
                    val list = todoMap[it.groupName] as MutableList
                    if (!TextUtils.isEmpty(it.content)) {
                        list.add(it)
                    }
                } else {
                    val list = ArrayList<ToDoData>()
                    if (!TextUtils.isEmpty(it.content)) {
                        list.add(it)
                    }
                    todoMap[it.groupName] = list
                }
            }
            // 更新Group
            groupList.clear()
            todoMap.mapValues {
                groupList.add(GroupListBean(it.key, it.value.size.toString()))
            }
            groupNameAdapter.setGroupData(groupList)
            if (groupList.size > 0 && (TextUtils.isEmpty(selectedGroup))) {
                val groupName = groupList[groupList.size - 1].groupName
                if (!TextUtils.isEmpty(groupName)) {
                    changeSelectedGroup(groupName)
                }
            }
            // 更新todoList
            refreshTodoList()
        }
    }

    private fun changeSelectedGroup(groupName: String) {
        Log.d(TAG, "changeSelectedGroup: groupName:$groupName")
        selectedGroup = groupName
        groupNameAdapter.setSelectGroup(groupName)
        refreshTodoList()
    }

    private fun refreshTodoList() {
        Log.d(TAG, "refreshTodoList: selectedGroup:$selectedGroup todoMap=${todoMap.size}")

        if (TextUtils.isEmpty(selectedGroup)) {
            return
        }
        todoList.clear()
        if (todoMap.isEmpty()) {
            todoListAdapter.setTodoData(todoList)
        } else {
            todoMap.mapValues {
                if (it.key == selectedGroup) {
                    Log.d(TAG, "refreshTodoList: it.value:${it.value}")
                    todoList.addAll(it.value)
                    todoListAdapter.setTodoData(todoList)
                }
            }
        }
    }

    /**
     * 初始化组名RecyclerView
     */
    private fun initGroupNameRv() {
        mBinding.groupListRv.layoutManager = LinearLayoutManager(this)
        mBinding.groupListRv.adapter = groupNameAdapter
    }

    private fun initTodoListRv() {
        mBinding.todoListRv.layoutManager = LinearLayoutManager(this)
        mBinding.todoListRv.adapter = todoListAdapter
    }

    private fun initCreateTodoEditText() {
        mBinding.createTodoEt.setOnFocusChangeListener { view, hasfocus ->
            if (hasfocus) {
                mBinding.addTodoHintLl.visibility = GONE
            } else if (TextUtils.isEmpty(mBinding.createTodoEt.text)) {
                mBinding.addTodoHintLl.visibility = VISIBLE
            }

        }
        mBinding.createTodoEt.setOnEditorActionListener(object : OnEditorActionListener {
            override fun onEditorAction(
                textView: TextView?,
                actionId: Int,
                keyEvent: KeyEvent?
            ): Boolean {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    imm.hideSoftInputFromWindow(textView?.windowToken, 0)
                    mBinding.createTodoEt.clearFocus()
                    mBinding.addTodoHintLl.visibility = VISIBLE
                    val etText = mBinding.createTodoEt.text
                    if (TextUtils.isEmpty(etText)) {
                        return true
                    }
                    if (TextUtils.isEmpty(selectedGroup)) {
                        mBinding.createTodoEt.text.clear()
                        Toast.makeText(
                            this@MainActivity,
                            getString(R.string.create_list_tips),
                            Toast.LENGTH_SHORT
                        ).show()
                        return true
                    }
                    todoViewModel.insertData(
                        ToDoData(0, groupName = selectedGroup, content = etText.toString())
                    )
                    mBinding.createTodoEt.text.clear()
                    return true
                } else {
                    return false
                }
            }

        })
    }

    override fun onClick(view: View?) {
        when (view) {
            mBinding.closeIv -> {
                updateWidget()
                finish()
            }

            mBinding.createListTv, mBinding.createListIv -> {
                createNewGroup()
            }

            mBinding.confirmTv -> {
                mBinding.pickerLl.visibility = GONE
                currentTodoData?.let {
                    it.remindTime = DateUtils.getDateString(mBinding.picker)
                    AlarmUtils.setAlarm(this, it.remindTime, it.content, it.id)
                    todoViewModel.updateData(it)
                }
            }

            mBinding.cancelTv -> {
                mBinding.pickerLl.visibility = GONE
            }

            mBinding.renameTv -> {
                groupNameAdapter.onGroupNameEdit()
                mBinding.moreEditView.visibility = INVISIBLE
            }

            mBinding.deleteTv -> {
                WarningDialogFragment.Builder()
                    .setTitle(getString(R.string.clear_data_title))
                    .setSummary(getString(R.string.clear_data_tip))
                    .setConfirmText(getString(R.string.delete))
                    .setCancelText(getString(R.string.cancel))
                    .setOnConfirmClickListener(object :
                        WarningDialogFragment.OnConfirmClickListener {
                        override fun onClick(view: View) {

                            val deleteGroup = selectedGroup
                            selectedGroup = ""
                            if (!TextUtils.isEmpty(deleteGroup) && todoMap.size == 1) {
                                changeSelectedGroup(deleteGroup)
                            }
                            todoViewModel.deleteGroup(deleteGroup)
                            mBinding.moreEditView.visibility = INVISIBLE
                            refreshTodoList()
                        }

                    })
                    .setOnCancelClickListener(object : WarningDialogFragment.OnCancelClickListener {
                        override fun onClick(view: View) {

                        }

                    }).builder().show(supportFragmentManager, "clear_data")
            }
        }
    }

    private fun createNewGroup() {
        var defGroupName = getString(R.string.list) + (groupList.size + 1)
        while (true) {
            // 防止命名重复
            groupList.forEach {
                if (it.groupName == defGroupName) {
                    defGroupName = defGroupName + "（1）"
                    return@forEach
                }
            }
            break
        }
        val todoData = ToDoData(0, groupName = defGroupName)
        todoViewModel.insertData(todoData)
        changeSelectedGroup(defGroupName)
        mBinding.createTodoEt.requestFocus()
        Log.d(TAG, "createNewGroup: insertData:$todoData")
    }

    override fun onGroupSelectChange(groupName: String) {
        changeSelectedGroup(groupName)
    }

    override fun onChangeGroupName(oldName: String, newName: String) {
        selectedGroup = newName
        todoViewModel.updateGroupName(oldName, newName)
    }

    override fun onClickMoreEdit(y: Int) {
        val maxY = ScreenUtils.getScreenHeight(this) - 400f
        mBinding.moreEditView.y = y.toFloat() - 50f
        if (maxY < mBinding.moreEditView.y) {
            mBinding.moreEditView.y = maxY
        }
        mBinding.moreEditView.visibility =
            if (mBinding.moreEditView.isVisible) INVISIBLE else VISIBLE
        mBinding.moreEditView.requestLayout()
    }

    override fun checkCanRenameGroupName(newName: String): Boolean {
        return todoViewModel.checkCanRenameGroupName(newName)
    }

    override fun onDeleteTodoData(data: ToDoData) {
        WarningDialogFragment.Builder()
            .setTitle(getString(R.string.clear_data_title))
            .setSummary(getString(R.string.clear_data_tip))
            .setConfirmText(getString(R.string.delete))
            .setCancelText(getString(R.string.cancel))
            .setOnConfirmClickListener(object : WarningDialogFragment.OnConfirmClickListener {
                override fun onClick(view: View) {
                    AlarmUtils.removeAlarm(this@MainActivity, data.id)
                    todoViewModel.deleteItem(data)
                }

            })
            .setOnCancelClickListener(object : WarningDialogFragment.OnCancelClickListener {
                override fun onClick(view: View) {

                }

            }).builder().show(supportFragmentManager, "clear_data_item")

    }

    override fun onCheckedDone(data: ToDoData) {
        if (data.hasDone) {
            AlarmUtils.removeAlarm(this, data.id)
        } else {
            AlarmUtils.setAlarm(this, data.remindTime, data.content, data.id)
        }
        todoViewModel.updateData(data)
    }

    override fun onClickAlarm(data: ToDoData, y: Int) {
        if (!NotificationUtils.areNotificationsEnabled(this)) {
            val applicationInfo = applicationInfo
            try {
                val intent = Intent()
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intent.action = "android.settings.APP_NOTIFICATION_SETTINGS"
                intent.putExtra("app_package", applicationInfo.packageName)
                intent.putExtra("android.provider.extra.APP_PACKAGE", applicationInfo.packageName)
                intent.putExtra("app_uid", applicationInfo.uid)
                startActivity(intent)
            } catch (e: Exception) {
                val intent = Intent()
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intent.action = "android.settings.APPLICATION_DETAILS_SETTINGS"
                intent.data = Uri.fromParts("package", applicationInfo.packageName, null)
                startActivity(intent)
            }
            return
        }
        mBinding.picker.setMinMillisecond(System.currentTimeMillis())
        currentTodoData = data
        val dateTimePickerMaxY = ScreenUtils.getScreenHeight(this) - 800f
        if (y > dateTimePickerMaxY) {
            mBinding.pickerLl.y = dateTimePickerMaxY
        } else {
            mBinding.pickerLl.y = y.toFloat() - 100f
        }
        mBinding.pickerLl.visibility = VISIBLE
        Log.d(
            TAG,
            "onClickAlarm: y:$y pickerLl.y:${mBinding.pickerLl.y} dateTimePickerMaxY:$dateTimePickerMaxY"
        )
    }

    private fun updateWidget() {
        if (widgetId == null || widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            return
        }
        SharedPreferenceUtils.saveWidgetData(this, selectedGroup)
        TodoWidgetProvider.updateAllWidget(this)
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (MotionEvent.ACTION_UP == ev?.action) {
            mBinding.moreEditView.visibility = GONE
        }
        return super.dispatchTouchEvent(ev)
    }
}