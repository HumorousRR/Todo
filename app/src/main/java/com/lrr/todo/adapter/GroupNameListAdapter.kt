package com.lrr.todo.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter4.BaseQuickAdapter
import com.lrr.todo.GlobalApplication
import com.lrr.todo.R
import com.lrr.todo.data.models.GroupListBean
import com.lrr.todo.databinding.ItemGroupNameBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GroupNameListAdapter(groupNameListChangeListener: GroupNameListChangeListener) :
    BaseQuickAdapter<GroupListBean, GroupNameListAdapter.GroupNameViewHolder>() {

    companion object {
        private const val TAG = "GroupNameListAdapter"
    }

    private val mGroupNameListChangeListener = groupNameListChangeListener
    private var mSelectGroupName = ""
    private var isEditMode = false
    private val imm by lazy {
        context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    }

    private var data = ArrayList<GroupListBean>()


    inner class GroupNameViewHolder(private val binding: ItemGroupNameBinding) :
        RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("UseCompatLoadingForDrawables")
        fun bind(item: GroupListBean) {
            binding.groupData = item

            binding.rootCl.setOnClickListener {
                imm.hideSoftInputFromWindow(binding.groupNameEt.windowToken, 0)
                mGroupNameListChangeListener.onGroupSelectChange(item.groupName)
            }

            binding.moreIv.setOnClickListener {
                val location = IntArray(2)
                binding.rootCl.getLocationInWindow(location)
                mGroupNameListChangeListener.onClickMoreEdit(location[1])
                Log.d(TAG, "bind: y:${location[1]}")
            }

            binding.groupNameEt.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
                    if (s == null) {
                        return
                    }
                    if (s.length > 9) {
                        binding.groupNameEt.setText(s.subSequence(0, 9))
                        binding.groupNameEt.setSelection(9)
                        Toast.makeText(
                            context,
                            context.getString(R.string.group_name_length_limit_tips),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            })

            binding.groupNameEt.setOnEditorActionListener(object :
                TextView.OnEditorActionListener {
                override fun onEditorAction(
                    textView: TextView?,
                    actionId: Int,
                    keyEvent: KeyEvent?
                ): Boolean {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        imm.hideSoftInputFromWindow(textView?.windowToken, 0)
                        binding.groupNameEt.clearFocus()
                        val etText = binding.groupNameEt.text
                        if (TextUtils.isEmpty(etText)) {
                            Toast.makeText(context, "列表名不能为空", Toast.LENGTH_SHORT).show()
                            return false
                        }
                        if (!mGroupNameListChangeListener.checkCanRenameGroupName(etText.toString())) {
                            Toast.makeText(GlobalApplication.getContextInstance(),context.resources.getString(R.string.group_name_duplicate_tips),Toast.LENGTH_SHORT).show()
                            return false
                        }
                        isEditMode = false
                        mGroupNameListChangeListener.onChangeGroupName(
                            mSelectGroupName,
                            etText.toString()
                        )
                        mSelectGroupName = etText.toString()
                        return true
                    } else {
                        return false
                    }
                }
            })

            if (mSelectGroupName == item.groupName) {
                binding.rootCl.background = context.getDrawable(R.drawable.bg_selected_group)
                binding.groupNameEt.setTextColor(context.getColor(R.color.white))
                binding.groupNameTv.setTextColor(context.getColor(R.color.white))
                binding.countTv.setTextColor(context.getColor(R.color.white))
                binding.groupNameEt.visibility = View.VISIBLE
                binding.moreIv.visibility = View.VISIBLE
                if (isEditMode) {
                    binding.groupNameEt.visibility = View.VISIBLE
                    binding.groupNameTv.visibility = View.GONE
                    binding.groupNameEt.postDelayed({
                        binding.groupNameEt.requestFocus()
                    }, 200)
                    showImm()
                } else {
                    binding.groupNameEt.visibility = View.GONE
                    binding.groupNameTv.visibility = View.VISIBLE
                    binding.groupNameEt.clearFocus()
                }
            } else {
                binding.rootCl.background = null
                binding.groupNameEt.setTextColor(context.getColor(R.color.black_666666))
                binding.groupNameTv.setTextColor(context.getColor(R.color.black_666666))
                binding.countTv.setTextColor(context.getColor(R.color.black_666666))
                binding.groupNameEt.visibility = View.GONE
                binding.groupNameTv.visibility = View.VISIBLE
                binding.moreIv.visibility = View.GONE
            }
            binding.executePendingBindings()
        }


        private fun showImm() {
            GlobalScope.launch(Dispatchers.IO) {
                delay(200)
                imm.showSoftInput(binding.groupNameEt, InputMethodManager.SHOW_IMPLICIT)
                binding.groupNameEt.setSelection(binding.groupNameEt.text.length)
            }
        }

    }

    override fun onBindViewHolder(
        holder: GroupNameViewHolder,
        position: Int,
        item: GroupListBean?
    ) {
        if (item != null) {
            holder.bind(item)
        }
    }

    override fun onCreateViewHolder(
        context: Context,
        parent: ViewGroup,
        viewType: Int
    ): GroupNameViewHolder {
        val binding =
            ItemGroupNameBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GroupNameViewHolder(binding)
    }


    interface GroupNameListChangeListener {
        fun onGroupSelectChange(groupName: String)

        fun onChangeGroupName(oldName: String, newName: String)

        fun onClickMoreEdit(y: Int)

        fun checkCanRenameGroupName(newName: String):Boolean

    }

    @SuppressLint("NotifyDataSetChanged")
    fun setSelectGroup(selectGroupName: String) {
        if (isEditMode) {
            isEditMode = false
        }
        this.mSelectGroupName = selectGroupName
        notifyDataSetChanged()
    }

    fun onGroupNameEdit() {
        isEditMode = true
        refreshCurrentItem()
    }

    fun setGroupData(mutableList: MutableList<GroupListBean>) {
        this.data.clear()
        this.data.addAll(mutableList)
        submitList(mutableList)
    }

    private fun refreshCurrentItem() {
        data.forEach {
            if (mSelectGroupName == it.groupName) {
                notifyItemChanged(data.indexOf(it))
            }
        }
    }
}