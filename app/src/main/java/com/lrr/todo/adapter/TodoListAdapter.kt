package com.lrr.todo.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Paint
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.BaseMultiItemAdapter
import com.lrr.todo.R
import com.lrr.todo.data.models.ToDoData
import com.lrr.todo.databinding.ItemDoneListBinding
import com.lrr.todo.databinding.ItemDoneTitleBinding
import com.lrr.todo.databinding.ItemTodoListBinding
import com.lrr.todo.listener.TodoListChangeListener
import com.lrr.todo.util.DateUtils

class TodoListAdapter(todoListChangeListener: TodoListChangeListener) :
    BaseMultiItemAdapter<ToDoData>() {

    private val mUndoneList = ArrayList<ToDoData>()

    private val mDoneList = ArrayList<ToDoData>()

    private val mTodoListChangeListener = todoListChangeListener

    private var mShowDoneList = false

    companion object {
        private const val TAG = "TodoListAdapter"
        private const val VIEW_TYPE_UNDONE = 0
        private const val VIEW_TYPE_DONE_TITLE = 1
        private const val VIEW_TYPE_DONE = 2
    }

    inner class TodoListViewHolder(private val binding: ItemTodoListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ToDoData) {
            binding.todoData = item
            binding.todoCb.isChecked = item.hasDone
            if (item.remindTime.isEmpty()) {
                binding.remindLl.visibility = View.GONE
            } else {
                binding.remindLl.visibility = View.VISIBLE
                if (DateUtils.hasReachedTime(item.remindTime)) {
                    binding.clockIv.setColorFilter(context.getColor(R.color.orange_FFA63C))
                    binding.remindTimeTv.setTextColor(context.getColor(R.color.orange_FFA63C))
                } else {
                    binding.clockIv.setColorFilter(context.getColor(R.color.black_666666))
                    binding.remindTimeTv.setTextColor(context.getColor(R.color.black_666666))
                }
            }
            initListener(item)
            binding.executePendingBindings()
        }

        @SuppressLint("NotifyDataSetChanged")
        private fun initListener(data: ToDoData) {
            binding.deleteIv.setOnClickListener {
                mTodoListChangeListener.onDeleteTodoData(data)
                notifyDataSetChanged()
            }
            binding.todoCb.setOnClickListener {
                if (binding.todoCb.isChecked) {
                    // CheckBox 被选中
                    data.hasDone = true
                    mTodoListChangeListener.onCheckedDone(data)
                } else {
                    // CheckBox 取消选中
                    data.hasDone = false
                    mTodoListChangeListener.onCheckedDone(data)
                }
                Log.d(TAG, "initListener: data:${data}")
            }
            binding.setClockIv.setOnClickListener {
                val location = IntArray(2)
                binding.root.getLocationInWindow(location)
                var y =
                    location[1] + binding.root.height - context.resources.getDimensionPixelSize(R.dimen.date_time_picker_offset)
                mTodoListChangeListener.onClickAlarm(data, y)
            }
        }
    }

    inner class DoneListViewHolder(private val binding: ItemDoneListBinding) :
        RecyclerView.ViewHolder(binding.root) {


        fun bind(item: ToDoData) {
            binding.todoData = item
            binding.todoCb.isChecked = item.hasDone
            binding.todoTv.paint.flags = Paint.STRIKE_THRU_TEXT_FLAG
            binding.todoTv.paint.isAntiAlias = true
            binding.rootCl.visibility = if (mShowDoneList) View.VISIBLE else View.GONE
            if (item.remindTime.isEmpty()) {
                binding.remindLl.visibility = View.GONE
            } else {
                binding.remindLl.visibility = View.VISIBLE
                binding.clockIv.setColorFilter(context.getColor(R.color.gray_c6c6c6))
            }
            initListener(item)
            binding.executePendingBindings()
        }

        @SuppressLint("NotifyDataSetChanged")
        private fun initListener(data: ToDoData) {
            binding.deleteIv.setOnClickListener {
                mTodoListChangeListener.onDeleteTodoData(data)
                notifyDataSetChanged()
            }
            binding.todoCb.setOnClickListener {
                if (binding.todoCb.isChecked) {
                    // CheckBox 被选中
                    data.hasDone = true
                    mTodoListChangeListener.onCheckedDone(data)
                } else {
                    // CheckBox 取消选中
                    data.hasDone = false
                    mTodoListChangeListener.onCheckedDone(data)
                }
                Log.d(TAG, "initListener: data:${data}")
            }
        }
    }

    inner class DoneTitleViewHolder(private val binding: ItemDoneTitleBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind() {
            if (mDoneList.size == 0) {
                binding.rootLl.visibility = View.GONE
            } else {
                binding.rootLl.visibility = View.VISIBLE
            }
            binding.arrowIv.setImageResource(if (mShowDoneList) R.drawable.ic_arrow_up else R.drawable.ic_arrow_down)
            initListener()
            binding.executePendingBindings()
        }

        private fun initListener() {
            binding.rootLl.setOnClickListener {
                mShowDoneList = !mShowDoneList
                notifyItemRangeChanged(mUndoneList.size, itemCount)
            }
        }
    }

    fun setTodoData(mutableList: MutableList<ToDoData>) {
        mUndoneList.clear()
        mDoneList.clear()
        mutableList.forEach { data ->
            if (TextUtils.isEmpty(data.content)) {
                mutableList.remove(data)
                return@forEach
            }
            if (data.hasDone) {
                mDoneList.add(data)
            } else {
                mUndoneList.add(data)
            }
        }
        val newList = ArrayList<ToDoData>()
        newList.addAll(mUndoneList)
        if (mDoneList.size > 0) {
            newList.add(ToDoData(id = 0, groupName = ""))
            newList.addAll(mDoneList)
        }
        submitList(newList)
        Log.d(
            TAG,
            "setTodoData: mutableList:$mutableList mUndoneList:$mUndoneList mDoneList:$mDoneList"
        )
    }

    // 在 init 初始化的时候，添加多类型
    init {
        addItemType(
            VIEW_TYPE_UNDONE,
            object : OnMultiItemAdapterListener<ToDoData, TodoListViewHolder> {
                override fun onCreate(
                    context: Context,
                    parent: ViewGroup,
                    viewType: Int
                ): TodoListViewHolder {
                    val viewBinding =
                        ItemTodoListBinding.inflate(LayoutInflater.from(context), parent, false)
                    return TodoListViewHolder(viewBinding)
                }

                override fun onBind(
                    holder: TodoListViewHolder,
                    position: Int,
                    item: ToDoData?
                ) {
                    if (item != null) {
                        holder.bind(item)
                    }
                }
            }).addItemType(
            VIEW_TYPE_DONE_TITLE,
            object : OnMultiItemAdapterListener<ToDoData, DoneTitleViewHolder> {
                override fun onCreate(
                    context: Context,
                    parent: ViewGroup,
                    viewType: Int
                ): DoneTitleViewHolder {
                    val viewBinding =
                        ItemDoneTitleBinding.inflate(
                            LayoutInflater.from(context),
                            parent,
                            false
                        )
                    return DoneTitleViewHolder(viewBinding)
                }

                override fun onBind(
                    holder: DoneTitleViewHolder,
                    position: Int,
                    item: ToDoData?
                ) {
                    if (item != null) {
                        holder.bind()
                    }
                }

            }).addItemType(
            VIEW_TYPE_DONE,
            object : OnMultiItemAdapterListener<ToDoData, DoneListViewHolder> {
                override fun onCreate(
                    context: Context,
                    parent: ViewGroup,
                    viewType: Int
                ): DoneListViewHolder {
                    val viewBinding =
                        ItemDoneListBinding.inflate(LayoutInflater.from(context), parent, false)
                    return DoneListViewHolder(viewBinding)
                }

                override fun onBind(
                    holder: DoneListViewHolder,
                    position: Int,
                    item: ToDoData?
                ) {
                    if (item != null) {
                        holder.bind(item)
                    }
                }

            }).onItemViewType { position, _ -> // 根据position，返回对应的 ItemViewType
            if (position < mUndoneList.size) {
                VIEW_TYPE_UNDONE
            } else if (position == mUndoneList.size) {
                VIEW_TYPE_DONE_TITLE
            } else {
                VIEW_TYPE_DONE
            }
        }
    }
}