package com.aoliao.example.factory.presenter.search;

import android.support.annotation.StringRes;

import com.aoliao.example.factory.data.DataSource;
import com.aoliao.example.factory.data.helper.UserHelper;
import com.aoliao.example.factory.model.card.UserCard;
import com.aoliao.example.factory.presenter.BasePresenter;

import net.qiujuer.genius.kit.handler.Run;
import net.qiujuer.genius.kit.handler.runable.Action;

import java.util.List;

import retrofit2.Call;

/**
 * 搜索人的实现
 * @author 你的奥利奥
 * @version 2017/8/19
 */

public class SearchUserPresenter extends BasePresenter<SearchContract.UserView>
        implements SearchContract.Presenter,DataSource.Callback<List<UserCard>> {
    private Call searchCall;
    public SearchUserPresenter(SearchContract.UserView view) {
        super(view);
    }

    @Override
    public void search(String content) {
        start();
        Call call=searchCall;
        //如果有上次的请求并且没有取消，则调用取消请求操作
        if (call!=null&&call.isCanceled()){
            call.cancel();
        }
        searchCall=UserHelper.search(content,this);
    }

    @Override
    public void onDataLoaded(final List<UserCard> userCards) {
        final SearchContract.UserView view=getView();
        if (view!=null){
            Run.onUiAsync(new Action() {
                @Override
                public void call() {
                    view.onSearchDone(userCards);
                }
            });
        }
    }

    @Override
    public void onDataNotAvailable(@StringRes final int strRes) {
        final SearchContract.UserView view=getView();
        if (view!=null){
            Run.onUiAsync(new Action() {
                @Override
                public void call() {
                    view.showError(strRes);
                }
            });
        }
    }
}
