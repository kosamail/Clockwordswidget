package com.andrey.kostin.clockwordswidget;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
//import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.andrey.kostin.timewidget.R;

//import java.util.Locale;

import yuku.ambilwarna.AmbilWarnaDialog;


public class Config extends Activity {

    int widgetID = AppWidgetManager.INVALID_APPWIDGET_ID;
    Intent resultValue;

    final String LOG_TAG = "MYLOG"; //имя тага для фильтрации логов

    public final static String WIDGET_PREF = "widget_pref";
    public final static String WIDTIME = "widtime";
    public final static String WIDDATE = "widdate";

    public final static String TEXT_COLOR = "widget_color_";    //переменная цвета шрифта
    public final static String BACK_COLOR = "back_color_";      //переменная фонового цвета лайота
    public final static String ALARM_FLAG = "alarm_flag_";      //переменная флаг для определения способа вызова будильника
    public final static String PAKAGE_NAME = "pakage_name_";    //переменная для хранения имени пакаджа для вызова будильника
    public final static String CLASS_NAME = "class_name_";      //переменная для хранения имени класса для вызова будильника
    public final static String LANG_FLAG = "lang_flag_";        //переменная флаг для определения языка локали смартфона
    public final static String TRANSP_FLAG = "transp_flag_";    //переменная флаг для установки прозрачного фона виджета
    public final static String FONT_TYPE = "font_type_";        //переменная для хранения типа шрифта надписей

    public String widtime="",widdate="";
    int color = 0xffffffff;     //переменная для задания цвета текта
    int colorback = 0x8e585858; //переменная для задания цвета фона
    //int color =Color.WHITE;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate config");
        requestWindowFeature(Window.FEATURE_NO_TITLE);//не отображать заголовок активити

        // извлекаем ID конфигурируемого виджета
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            widgetID = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        // и проверяем его корректность
        if (widgetID == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }

        // формируем intent ответа
        resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);

        // отрицательный ответ
        setResult(RESULT_CANCELED, resultValue);

        setContentView(R.layout.config); //Указываем контент из хмл файла

        //Задаем шрифт для радиобуттонов. Важно - задавать можно только после сетконтентвью
        RadioButton radio=(RadioButton)findViewById(R.id.rone);
        Typeface font = Typeface.createFromAsset(getAssets(), "sansita.ttf");
        radio.setTypeface(font);

        radio=(RadioButton)findViewById(R.id.rtwo);
        font = Typeface.createFromAsset(getAssets(), "unisans.otf");
        radio.setTypeface(font);

        radio=(RadioButton)findViewById(R.id.rthree);
        font = Typeface.createFromAsset(getAssets(), "cupbold.ttf");
        radio.setTypeface(font);
    }

    //Метод в котором вызываем диалог выбора цвета для текста
    void openDialog(boolean supportsAlpha, final boolean target) {
        AmbilWarnaDialog dialog = new AmbilWarnaDialog(Config.this, color, supportsAlpha, new AmbilWarnaDialog.OnAmbilWarnaListener() {
            @Override
            public void onOk(AmbilWarnaDialog dialog, int color) {
                ImageView ivcircle;
                if(target)  {Config.this.color = color;                                   // полученный цвет присваиваем переменной цвета текста
                             ivcircle =(ImageView)findViewById(R.id.ivcircle);}           // связываем с переменной для доступа к shape текстового кружка
                        else{Config.this.colorback = color;
                    //Config.this.colorback = Color.argb(190, Color.red(color), Color.green(color), Color.blue(color));// полученному цвету присваиваем альфа прозрачность 190 и присваиваем переменной цвета фона
                             ivcircle =(ImageView)findViewById(R.id.ivcircleback);}       // связываем с переменной для доступа к shape фонового кружка
                ivcircle.setColorFilter(color);                              // изменяем цвет у круглого shape
                //((GradientDrawable)tvcircle.getBackground()).setColor(color);// изменяем цвет фона у shape в случае если он назначен бекграундом
            }
            @Override
            public void onCancel(AmbilWarnaDialog dialog) {}
        });
        dialog.show();
    }

    //Функция обработчик нажатий
    public void onClick(View v) {

        //Вызываем диалог выбора цвета по нажатии на круг с текстом

        switch (v.getId()){       //если нажали на текст или круг  запустить метод вызова диалога выбора цвета
                case R.id.tvtext:
                case R.id.ivcircle:openDialog(true,true);break;
                case R.id.tvtextback:
                case R.id.ivcircleback:openDialog(true,false);break;
                default:break;
        }

        //Определяем состояние чекбокса
        CheckBox checkzeroback=(CheckBox)findViewById(R.id.checkzeroback);      //привязываемся к чекбоксу чекзеробек

        boolean transparent;
        if(checkzeroback.isChecked()) {transparent = true;} //в зависимости от состояния чекбокса задаем прозрачный бекграунд если 1
        else {transparent = false;}                         // не меняем бекграунд если 0

/*
        int layoutbackground;                                                   //переменная для выбора бекграунда лайота
        if(checkzeroback.isChecked()) {layoutbackground = R.drawable.backzero;} //в зависимости от состояния чекбокса задаем прозрачный бекграунд если 1
                                else {layoutbackground = R.drawable.back;}      // задаем серый бекграунд если 0
*/


        //Определяем какой шрифт был выбран в радиогруппе
        int selfont = ((RadioGroup) findViewById(R.id.rgfont)).getCheckedRadioButtonId();
        String fonttypeface="sansita.ttf";
        switch (selfont) {
            case R.id.rone:
                fonttypeface = "sansita.ttf";
                break;
            case R.id.rtwo:
                fonttypeface = "unisans.otf";
                break;
            case R.id.rthree:
                fonttypeface = "cupbold.ttf";
                break;
        }

/*      //Часть кода для установки времени и даты в конфигурационном активити - не применяю сейчас так как делаю это в активити виджета
        DateFormat df = new SimpleDateFormat("HH:mm"); //Задаем формат времени  09:46
        widtime = df.format(Calendar.getInstance().getTime());     //передаем время в строковую переменную

        df = new SimpleDateFormat("EEE, d MMM"); //Задаем формат даты           среда, 9 декабря
        widdate = df.format(Calendar.getInstance().getTime());     //передаем дату-время в строковую переменную
*/

/*      //пример формата дат и времени
        "yyyy.MM.dd G 'at' HH:mm:ss z" ---- 2001.07.04 AD at 12:08:56 PDT
        "hh 'o''clock' a, zzzz" ----------- 12 o'clock PM, Pacific Daylight Time
        "EEE, d MMM yyyy HH:mm:ss Z"------- Wed, 4 Jul 2001 12:08:56 -0700
        "yyyy-MM-dd'T'HH:mm:ss.SSSZ"------- 2001-07-04T12:08:56.235-0700
        "yyMMddHHmmssZ"-------------------- 010704120856-0700
        "K:mm a, z" ----------------------- 0:08 PM, PDT
        "h:mm a" -------------------------- 12:08 PM
        "EEE, MMM d, ''yy" ---------------- Wed, Jul 4, '01*/

        //EditText ettime = (EditText) findViewById(R.id.ettime); //Привязываемся к эдиттексту
        //ettime.setText(wtime); //передаем время в эдит текст - не нужно далее передаю напрямую через эдитор путстринг

 //!!!!Определение языка системы
        String locale = getResources().getConfiguration().locale.toString(); //Берем локаль в строковую переменную. Можно еще так взять язык экрана: String language = Locale.getDefault().getDisplayLanguage();
        Boolean foundRU = false; //Переменная для определения языка локали Русский или нет. Инициализируем переменную ложью, предполагая что локаль не русская
        //Log.d(LOG_TAG, "locale= "+locale);
        if (locale.equals("ru_RU")){foundRU = true;}        //если язык локали равен RU, заносим в переменную флаг истина. Можно еще язык сравнивать: if (language.equalsIgnoreCase("русский")){}// выполняем что-то

        //Настройка вызова встроенного приложения часов-будильника по нажатию на часы или время следующего будильника
        Context context=getApplicationContext();
        PackageManager packageManager = context.getPackageManager();
        Intent alarmClockIntent = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER);
        // Поиск встроенного приложения будильника
        String clockImpls[][] = {                           //список известных встроенных приложений по производителям
                {"HTC Alarm Clock", "com.htc.android.worldclock", "com.htc.android.worldclock.WorldClockTabControl" },
                {"Standar Alarm Clock", "com.android.deskclock", "com.android.deskclock.AlarmClock"},
                {"Standar Alarm Clock2", "com.google.android.deskclock", "com.android.deskclock.AlarmClock"},
                {"Froyo Nexus Alarm Clock", "com.google.android.deskclock", "com.android.deskclock.DeskClock"},
                {"Moto Blur Alarm Clock", "com.motorola.blur.alarmclock",  "com.motorola.blur.alarmclock.AlarmClock"},
                {"Samsung Galaxy Clock", "com.sec.android.app.clockpackage","com.sec.android.app.clockpackage.ClockPackage"} ,
                {"Sony Ericsson Xperia Z", "com.sonyericsson.organizer", "com.sonyericsson.organizer.Organizer_WorldClock" },
                {"ASUS Tablets", "com.asus.deskclock", "com.asus.deskclock.DeskClock"},
                {"LG Alarm Clock", "com.lge.clock", "com.lge.clock.AlarmClockActivity"}        };

        Boolean foundClockImpl = false;                         //инициализируем переменную ложью, если что-то найдется будет далее истина

        SharedPreferences sp = getSharedPreferences(WIDGET_PREF, MODE_PRIVATE); //берем шаредпрефененсес для дальнейшего хранении информации о встроенном приложении будильника, выбранном цвете и фоне
        Editor editor = sp.edit();                              //создаем эдитор для записи значений в шаредпреференсес

        for(int i=0; i<clockImpls.length; i++) {                //цикл в котором будем подберать известное встроенное приложени
            String vendor =      clockImpls[i][0];              //переменная для имени производителя телефона
            String packageName = clockImpls[i][1];              //переменная для имени пакета приложения будильника
            String className =   clockImpls[i][2];              //переменная для имени класса приложения будильника
            try {                                                               //делаем попытку:
                ComponentName cn = new ComponentName(packageName, className);   //создаем новое имя компонента
                ActivityInfo aInfo = packageManager.getActivityInfo(cn, PackageManager.GET_META_DATA);//берем активитиинфо
                alarmClockIntent.setComponent(cn);              //здесь в интент помещаем имя пакаджа будильника и класса будильника для дальнейшего использования
                Log.d(LOG_TAG, "Found " + vendor + " -> " + packageName + "/" + className);
                foundClockImpl = true;                  //здесь в переменную флаг приложения будильника помещаем 1 - мы его нашли и можем использовать далее
                editor.putString(PAKAGE_NAME + widgetID, packageName);   //записываем в шаредпреференсес один для всех пакаджнейм будильника
                editor.putString(CLASS_NAME + widgetID, className);      //записываем в шаредпреференсес один для всех класснейм будильника
//              editor.commit();                                     //сохраняем значения в шаредпреференсес
            } catch (PackageManager.NameNotFoundException e) {Log.d(LOG_TAG, "No "+ vendor);}
        }
        editor.putBoolean(ALARM_FLAG + widgetID, foundClockImpl);//записываем в шаредпреференсес флаг приложения будильника
        editor.putBoolean(LANG_FLAG + widgetID, foundRU);       //записываем в шаредпреференсес флаг языка локали 1-RU 0-все остальные
        editor.putBoolean(TRANSP_FLAG + widgetID, transparent); //записываем в шаредпреференсес флаг установки прозрачности фона виджета 1-прозрачен 0-не прозрачен

        // Записываем значения с экрана в Preferences
//        SharedPreferences sp = getSharedPreferences(WIDGET_PREF, MODE_PRIVATE);
//        Editor editor = sp.edit();

        editor.putString(FONT_TYPE + widgetID, fonttypeface);   //задаем тип шрифта для надписей
//        editor.putInt(BACK_COLOR + widgetID, layoutbackground); //задаем бекграунд виджета ид хмл бекграунда
        editor.putInt(BACK_COLOR + widgetID, colorback); //задаем бекграунд виджета цветом
        editor.putInt(TEXT_COLOR + widgetID, color);            //задаем цвет текста виджета
        editor.commit();                                        //сохраняем значения в шаредпреференсес

/*      //Передаем виджету время и дату - сейчас эти преференсес не использую потому как время и дату задаю напрямую из виджета
        editor.putString(WIDGET_TIME + widgetID, ettime.getText().toString()); //передаем виджету значение из эдиттекста - не использую
        editor.putString(WIDTIME + widgetID, widtime); //!!! именно здесь передаем виджету время !!!
        editor.putString(WIDDATE + widgetID, widdate); //!!! именно здесь передаем виджету дату !!!
*/
        if(v.getId()==R.id.button){
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        Clockwordswidget.updateWidget(this, appWidgetManager, sp, widgetID);
        // положительный ответ
        setResult(RESULT_OK, resultValue);
        Log.d(LOG_TAG, "finish config " + widgetID);
        finish();
        }
    }
}
