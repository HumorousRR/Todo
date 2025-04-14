package com.lrr.todo.data.models

class GroupListBean(
    /**
     * 分组名称
     */
    var groupName: String,
    /**
     * 该分组内部todoList个数
     */
    var listCount: String,
) {

    override fun toString(): String {
        return "groupName:$groupName listCount:$listCount"
    }
}