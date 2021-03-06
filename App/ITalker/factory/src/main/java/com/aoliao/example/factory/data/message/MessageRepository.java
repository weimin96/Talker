package com.aoliao.example.factory.data.message;

import android.support.annotation.NonNull;

import com.aoliao.example.factory.data.BaseDbRepository;
import com.aoliao.example.factory.model.db.Message;
import com.aoliao.example.factory.model.db.Message_Table;
import com.aoliao.example.factory.persistence.Account;
import com.raizlabs.android.dbflow.sql.language.OperatorGroup;
import com.raizlabs.android.dbflow.sql.language.SQLite;
import com.raizlabs.android.dbflow.structure.database.transaction.QueryTransaction;

import java.util.Collections;
import java.util.List;

/**
 * 跟某人聊天的时候的聊天列表
 * 关注的内容一定是我发给这个人的，或者是他发送给我的
 *
 * @author 你的奥利奥
 * @version 2017/8/23
 */

public class MessageRepository extends BaseDbRepository<Message> implements MessageDataSource {
    // 正在聊天的对象Id
    private String receiverId;

    public MessageRepository(String receiverId) {
        super();
        this.receiverId = receiverId;
    }

    @Override
    protected boolean isRequired(Message message) {
        // receiverId 如果是发送者，那么Group==null情况下一定是发送给我的消息
        // 如果消息的接收者不为空，那么一定是发送给某个人的，这个人只能是我或者是某个人
        // 如果这个"某个人"就是receiverId，那么就是我需要关注的信息
        return (receiverId.equalsIgnoreCase(message.getSender().getId())
                && message.getGroup() == null)
                || (message.getReceiver() != null
                && receiverId.equalsIgnoreCase(message.getReceiver().getId())
        );
    }

    @Override
    public void load(SucceedCallback<List<Message>> callback) {
        super.load(callback);
        //查找消息，与你聊天的人（receiverId）不是发送者就是接收者
        //(sender_id == receiverId and group_id == null)
        // or (receiver_id==receiverId)
        SQLite.select()
                .from(Message.class)
                .where(OperatorGroup.clause()
                        .and(Message_Table.sender_id.eq(receiverId))// 这条消息的发送者是对方
                        .and(Message_Table.group_id.isNull())       // 不是群
                        .and(Message_Table.account_id.eq(Account.getUserId())))
                .or(OperatorGroup.clause()
                        .and(Message_Table.receiver_id.eq(receiverId))
                        .and(Message_Table.account_id.eq(Account.getUserId())))
                .orderBy(Message_Table.createAt, false)
                .limit(30)
                .async()
                .queryListResultCallback(this)
                .execute();
    }

    @Override
    public void onListQueryResult(QueryTransaction transaction, @NonNull List<Message> tResult) {

        // 反转返回的集合
        Collections.reverse(tResult);
        // 然后再调度
        super.onListQueryResult(transaction, tResult);
    }
}
