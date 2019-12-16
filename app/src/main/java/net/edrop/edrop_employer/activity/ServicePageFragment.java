package net.edrop.edrop_employer.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.header.ClassicsHeader;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import net.edrop.edrop_employer.R;
import net.edrop.edrop_employer.adapter.AcceptOrderAdapter;
import net.edrop.edrop_employer.adapter.MyPagerAdapter;
import net.edrop.edrop_employer.entity.Order;
import net.edrop.edrop_employer.utils.Constant;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Android Studio.
 * User: zhanghaoyu
 * Date: 2019/12/12
 * Time: 16:25
 */
public class ServicePageFragment extends Fragment {
    private static final String SECTION_STRING = "fragment_string";
    private View view;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private LayoutInflater mInflater;
    private ListView listView1;
    private List<String> mTitleList = new ArrayList<String>();//页卡标题集合
    private View view1, view2, view3;//页卡视图
    private List<View> mViewList = new ArrayList<>();//页卡视图集合
    private OkHttpClient okHttpClient;
    private List<Order> orderList = null;
    //接单控件
    private SmartRefreshLayout refreshLayout1;
    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == -1) {
                String json = (String) msg.obj;
                orderList = new Gson().fromJson(json, new TypeToken<List<Order>>() {
                }.getType());
                refreshLayout1.finishRefresh();
                setMyAdapter();
//                Log.e("a", orderList.toString());
            }

        }


    };

    private void setRefreshListeners() {
        refreshLayout1.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                getAcceptOrdersByOkHttp();

            }
        });


    }

    public static ServicePageFragment newInstance(String sectionNumber) {
        ServicePageFragment fragment = new ServicePageFragment();
        Bundle args = new Bundle();
        args.putString(SECTION_STRING, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_service_page, container, false);

        initView();
        initTabLayout();
        setRefreshListeners();
        return view;
    }

    private void setMyAdapter() {

        AcceptOrderAdapter adapter = new AcceptOrderAdapter(orderList, view.getContext(), R.layout.item_acceptorders_layout);
        listView1.setAdapter(adapter);

    }

    private void setmTabLayoutListener() {
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.e("aaa", "切换" + tab.getText().toString());
                //选择时触发
                switch (tab.getPosition()) {
                    case 0:
                        Log.e("aaa", "切换到了" + tab.getText().toString());

                        getAcceptOrdersByOkHttp();
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                //未选择时触发
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                //选中之后再次点击即复选时触发
            }
        });


    }

    private void getAcceptOrdersByOkHttp() {
        FormBody formBody = new FormBody.Builder()
                .add("state", -1 + "").build();
        Request request = new Request.Builder()
                .url(Constant.BASE_URL + "getOrderByState")
                .post(formBody)
                .build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String string = response.body().string();

                Message message = new Message();
                message.what = -1;
                message.obj = string;
                handler.sendMessage(message);
            }
        });
    }

    private void initView() {
        mViewPager = (ViewPager) view.findViewById(R.id.order_vp_view);
        mTabLayout = (TabLayout) view.findViewById(R.id.order_tabs);
        mInflater = LayoutInflater.from(view.getContext());
        okHttpClient = new OkHttpClient();
    }

    private void initTabLayout() {


        view1 = mInflater.inflate(R.layout.item_service_accept_order, null);
        refreshLayout1 = view1.findViewById(R.id.accept_smart_layout);
        refreshLayout1.setRefreshHeader(new ClassicsHeader(view.getContext()));
        listView1 = view1.findViewById(R.id.lv_acceptOrders);
        view2 = mInflater.inflate(R.layout.item_service_wait_order, null);
        view3 = mInflater.inflate(R.layout.item_service_done_order, null);

        //添加页卡视图
        mViewList.add(view1);
        mViewList.add(view2);
        mViewList.add(view3);
        //添加页卡标题
        mTitleList.add("接单大厅");
        mTitleList.add("待服务");
        mTitleList.add("已完成");
        setmTabLayoutListener();
        //添加tab选项卡，默认第一个选中
        mTabLayout.addTab(mTabLayout.newTab().setText(mTitleList.get(0)), true);
        mTabLayout.addTab(mTabLayout.newTab().setText(mTitleList.get(1)));
        mTabLayout.addTab(mTabLayout.newTab().setText(mTitleList.get(2)));

        MyPagerAdapter mAdapter = new MyPagerAdapter(mViewList);
        //给ViewPager设置适配器
        mViewPager.setAdapter(mAdapter);
        //将TabLayout和ViewPager关联起来
        mTabLayout.setupWithViewPager(mViewPager);
        //给Tabs设置适配器
        mTabLayout.setTabsFromPagerAdapter(mAdapter);
    }

    /**
     * ViewPager适配器
     **/
    private class MyPagerAdapter extends PagerAdapter {
        private List<View> mViewList;

        public MyPagerAdapter(List<View> mViewList) {
            this.mViewList = mViewList;
        }

        @Override
        public int getCount() {
            return mViewList.size();//页卡数
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;//官方推荐写法
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(mViewList.get(position));//添加页卡
            return mViewList.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mViewList.get(position));//删除页卡
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTitleList.get(position);//页卡标题
        }

    }
}
