package com.huicheng;

import java.util.ArrayList;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.huicheng.ui.Ble_Activity;

/**
 * 特别说明：HC_BLE助手是广州汇承信息科技有限公司独自研发的手机APP，方便用户调试08蓝牙模块。
 * 本软件只能支持安卓版本4.3并且有蓝牙4.0的手机使用。
 * 另外对于自家的05、06模块，要使用另外一套蓝牙2.0的手机APP，用户可以在汇承官方网的下载中心自行下载。
 * 本软件提供代码和注释，免费给购买汇承08模块的用户学习和研究，但不能用于商业开发，最终解析权在广州汇承信息科技有限公司。
 * **/
/**
 * @Description: TODO<MainActivity类实现打开蓝牙、扫描蓝牙>
 * @author 广州汇承信息科技有限公司
 * @data: 2014-10-12 上午10:28:18
 * @version: V1.0
 */
public class MainActivity extends Activity implements OnClickListener {
	// 扫描蓝牙按钮
	private Button scan_btn;
	// 蓝牙适配器
	BluetoothAdapter mBluetoothAdapter;
	// 蓝牙信号强度
	private ArrayList<Integer> rssis;
	// 自定义Adapter
	LeDeviceListAdapter mleDeviceListAdapter;
	// listview显示扫描到的蓝牙信息
	ListView lv;
	// 描述扫描蓝牙的状态
	private boolean mScanning;
	private boolean scan_flag;
	private Handler mHandler;
	int REQUEST_ENABLE_BT = 1;
	// 蓝牙扫描时间
	private static final long SCAN_PERIOD = 10000;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		// 初始化控件
		init();
		// 初始化蓝牙
		init_ble();
		scan_flag = true;
		// 自定义适配器
		mleDeviceListAdapter = new LeDeviceListAdapter();
		// 为listview指定适配器
		lv.setAdapter(mleDeviceListAdapter);

		/* listview点击函数 */
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int position,
					long id)
			{
				// TODO Auto-generated method stub
				final BluetoothDevice device = mleDeviceListAdapter
						.getDevice(position);
				if (device == null)
					return;
				final Intent intent = new Intent(MainActivity.this,
						Ble_Activity.class);
				intent.putExtra(Ble_Activity.EXTRAS_DEVICE_NAME,
						device.getName());
				intent.putExtra(Ble_Activity.EXTRAS_DEVICE_ADDRESS,
						device.getAddress());
				intent.putExtra(Ble_Activity.EXTRAS_DEVICE_RSSI,
						rssis.get(position).toString());
				if (mScanning)
				{
					/* 停止扫描设备 */
					mBluetoothAdapter.stopLeScan(mLeScanCallback);
					mScanning = false;
				}

				try
				{
					// 启动Ble_Activity
					startActivity(intent);
				} catch (Exception e)
				{
					e.printStackTrace();
					// TODO: handle exception
				}

			}
		});

	}

	/**
	 * @Title: init
	 * @Description: TODO(初始化UI控件)
	 * @param
	 * @return void
	 * @throws
	 */
	private void init()
	{
		scan_btn = (Button) this.findViewById(R.id.scan_dev_btn);
		scan_btn.setOnClickListener(this);
		lv = (ListView) this.findViewById(R.id.lv);
		mHandler = new Handler();
	}

	/**
	 * @Title: init_ble
	 * @Description: TODO(初始化蓝牙)
	 * @param
	 * @return void
	 * @throws
	 */
	private void init_ble()
	{
		// 手机硬件支持蓝牙
		if (!getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_BLUETOOTH_LE))
		{
			Toast.makeText(this, "不支持BLE", Toast.LENGTH_SHORT).show();
			finish();
		}
		// Initializes Bluetooth adapter.
		// 获取手机本地的蓝牙适配器
		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();
		// 打开蓝牙权限
		if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled())
		{
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}

	}

	/*
	 * 按钮响应事件
	 */
	@Override
	public void onClick(View v)
	{
		// TODO Auto-generated method stub

		if (scan_flag)
		{
			mleDeviceListAdapter = new LeDeviceListAdapter();
			lv.setAdapter(mleDeviceListAdapter);
			scanLeDevice(true);
		} else
		{

			scanLeDevice(false);
			scan_btn.setText("扫描设备");
		}
	}

	/**
	 * @Title: scanLeDevice
	 * @Description: TODO(扫描蓝牙设备 )
	 * @param enable
	 *            (扫描使能，true:扫描开始,false:扫描停止)
	 * @return void
	 * @throws
	 */
	private void scanLeDevice(final boolean enable)
	{
		if (enable)
		{
			// Stops scanning after a pre-defined scan period.
			mHandler.postDelayed(new Runnable()
			{
				@Override
				public void run()
				{
					mScanning = false;
					scan_flag = true;
					scan_btn.setText("扫描设备");
					Log.i("SCAN", "stop.....................");
					mBluetoothAdapter.stopLeScan(mLeScanCallback);
				}
			}, SCAN_PERIOD);
			/* 开始扫描蓝牙设备，带mLeScanCallback 回调函数 */
			Log.i("SCAN", "begin.....................");
			mScanning = true;
			scan_flag = false;
			scan_btn.setText("停止扫描");
			mBluetoothAdapter.startLeScan(mLeScanCallback);
		} else
		{
			Log.i("Stop", "stoping................");
			mScanning = false;
			mBluetoothAdapter.stopLeScan(mLeScanCallback);
			scan_flag = true;
		}

	}

	/**
	 * 蓝牙扫描回调函数 实现扫描蓝牙设备，回调蓝牙BluetoothDevice，可以获取name MAC等信息
	 * 
	 * **/
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback()
	{

		@Override
		public void onLeScan(final BluetoothDevice device, final int rssi,
				byte[] scanRecord)
		{
			// TODO Auto-generated method stub

			runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					/* 讲扫描到设备的信息输出到listview的适配器 */
					mleDeviceListAdapter.addDevice(device, rssi);
					mleDeviceListAdapter.notifyDataSetChanged();
				}
			});

			System.out.println("Address:" + device.getAddress());
			System.out.println("Name:" + device.getName());
			System.out.println("rssi:" + rssi);

		}
	};

	/**
	 * @Description: TODO<自定义适配器Adapter,作为listview的适配器>
	 * @author 广州汇承信息科技有限公司
	 * @data: 2014-10-12 上午10:46:30
	 * @version: V1.0
	 */
	private class LeDeviceListAdapter extends BaseAdapter {
		private ArrayList<BluetoothDevice> mLeDevices;

		private LayoutInflater mInflator;

		public LeDeviceListAdapter()
		{
			super();
			rssis = new ArrayList<Integer>();
			mLeDevices = new ArrayList<BluetoothDevice>();
			mInflator = getLayoutInflater();
		}

		public void addDevice(BluetoothDevice device, int rssi)
		{
			if (!mLeDevices.contains(device))
			{
				mLeDevices.add(device);
				rssis.add(rssi);
			}
		}

		public BluetoothDevice getDevice(int position)
		{
			return mLeDevices.get(position);
		}

		public void clear()
		{
			mLeDevices.clear();
			rssis.clear();
		}

		@Override
		public int getCount()
		{
			return mLeDevices.size();
		}

		@Override
		public Object getItem(int i)
		{
			return mLeDevices.get(i);
		}

		@Override
		public long getItemId(int i)
		{
			return i;
		}

		/**
		 * 重写getview
		 * 
		 * **/
		@Override
		public View getView(int i, View view, ViewGroup viewGroup)
		{

			// General ListView optimization code.
			// 加载listview每一项的视图
			view = mInflator.inflate(R.layout.listitem, null);
			// 初始化三个textview显示蓝牙信息
			TextView deviceAddress = (TextView) view
					.findViewById(R.id.tv_deviceAddr);
			TextView deviceName = (TextView) view
					.findViewById(R.id.tv_deviceName);
			TextView rssi = (TextView) view.findViewById(R.id.tv_rssi);

			BluetoothDevice device = mLeDevices.get(i);
			deviceAddress.setText(device.getAddress());
			deviceName.setText(device.getName());
			rssi.setText("" + rssis.get(i));

			return view;
		}
	}

}
