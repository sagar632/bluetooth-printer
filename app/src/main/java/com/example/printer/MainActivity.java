package com.example.printer;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mazenrashed.printooth.Printooth;
import com.mazenrashed.printooth.data.printable.ImagePrintable;
import com.mazenrashed.printooth.data.printable.Printable;
import com.mazenrashed.printooth.data.printable.RawPrintable;
import com.mazenrashed.printooth.data.printable.TextPrintable;
import com.mazenrashed.printooth.data.printer.DefaultPrinter;
import com.mazenrashed.printooth.ui.ScanningActivity;
import com.mazenrashed.printooth.utilities.Printing;
import com.mazenrashed.printooth.utilities.PrintingCallback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import android.bluetooth.BluetoothAdapter;
import  android.bluetooth.BluetoothDevice;
import  android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.Set;
import java.util.UUID;
import android.os.Handler;

import java.util.concurrent.RunnableFuture;
import java.util.logging.LogRecord;
public class MainActivity extends AppCompatActivity implements PrintingCallback {

Button btn_text,btn_image,btn_pair_unpair;
    BluetoothAdapter bluetoothAdapter;
    BluetoothSocket bluetoothSocket;
    BluetoothDevice bluetoothDevice;
ImageView imageView;
    OutputStream outputStream;
    InputStream inputStream;
    Thread thread;
Printing printing;
    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;

    TextView lblPrinterName;
    EditText textBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
    }



    private void initView() {
        btn_text = findViewById(R.id.text);
        btn_image = findViewById(R.id.image);
        btn_pair_unpair = findViewById(R.id.submit);
        imageView=findViewById(R.id.imageview);
        Picasso.get().load(R.drawable.ss).resize(250,250).into(imageView);
        if (printing != null)
            printing.setPrintingCallback((PrintingCallback) this);

        btn_pair_unpair.setOnClickListener(view->{
            if (Printooth.INSTANCE.hasPairedPrinter())
                Printooth.INSTANCE.removeCurrentPrinter();
            else
            {

                startActivityForResult(new Intent(MainActivity.this, ScanningActivity.class),ScanningActivity.SCANNING_FOR_PRINTER);
                changePairandUnpair();
            }
        });
        btn_image.setOnClickListener(view->{
            if(!Printooth.INSTANCE.hasPairedPrinter())
                startActivityForResult(new Intent(MainActivity.this,ScanningActivity.class),ScanningActivity.SCANNING_FOR_PRINTER);
            else
                printImage();
        });



        btn_text.setOnClickListener(view->{
            if(!Printooth.INSTANCE.hasPairedPrinter())
                startActivityForResult(new Intent(MainActivity.this,ScanningActivity.class),ScanningActivity.SCANNING_FOR_PRINTER);
            else {

                printtext();

            }
        });
        changePairandUnpair();
    }

    private void printtext() {
        ArrayList<Printable>printables=new ArrayList<>();
        printables.add(new RawPrintable.Builder(new byte[]{10,10,1}).build());

        Picasso.get().load(R.drawable.nea).resize(200,200).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                printables.add(new ImagePrintable.Builder(bitmap).setNewLinesAfter(1).setAlignment(DefaultPrinter.Companion.getALIGNMENT_CENTER()).build());

            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });
      //  printables.add(new TextPrintable.Builder().setText("Hello World").setNewLinesAfter(1).build());
        printables.add(new TextPrintable.Builder().setText("Nepal Electricity Authority").setNewLinesAfter(1).setAlignment(DefaultPrinter.Companion.getALIGNMENT_CENTER()).setEmphasizedMode(DefaultPrinter.Companion.getEMPHASIZED_MODE_BOLD()).setUnderlined(DefaultPrinter.Companion.getUNDERLINED_MODE_ON()).build());
        Printooth.INSTANCE.printer().print(printables);


    }

    private void printImage() {
        ArrayList<Printable> printables = new ArrayList<Printable>();
        Picasso.get().load(R.drawable.nea).resize(250,250).into(new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                printables.add(new ImagePrintable.Builder(bitmap).build());
                printables.add(new TextPrintable.Builder().setText("Hello World").setNewLinesAfter(1).build());
                Printooth.INSTANCE.printer().print(printables);
            }

            @Override
            public void onBitmapFailed(Exception e, Drawable errorDrawable) {

            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {

            }
        });

       
    }

    private void changePairandUnpair() {
if(Printooth.INSTANCE.hasPairedPrinter())
    btn_pair_unpair.setText(new StringBuilder("Unpair ").append(Printooth.INSTANCE.getPairedPrinter().getName().toString()));
else
    btn_pair_unpair.setText("Pair with Printer");

    }


    @Override
    public void connectingWithPrinter() {
        Toast.makeText(this,"connecting",Toast.LENGTH_SHORT).show();
    }

    @Override
    public void connectionFailed(String s) {
Toast.makeText(this,"Failed"+s,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onError(String s) {
Toast.makeText(this,"Error"+s,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMessage(String s) {
Toast.makeText(this,s,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void printingOrderSentSuccessfully() {
Toast.makeText(this,"Order sent",Toast.LENGTH_SHORT).show();

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==ScanningActivity.SCANNING_FOR_PRINTER &&
        resultCode== Activity.RESULT_OK)
            initprinting();
        changePairandUnpair();
    }

    private void initprinting() {
if(Printooth.INSTANCE.hasPairedPrinter())
    printing=Printooth.INSTANCE.printer();
if(printing!=null)
    printing.setPrintingCallback(this);


    }
}
