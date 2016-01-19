package com.andrey.kostin.timewidget;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.RadioGroup;


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

    public String widtime="",widdate="";


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

        setContentView(R.layout.config);
    }

    //Функция обработчик нажатий
    public void onClick(View v) {
        int selectColor = ((RadioGroup) findViewById(R.id.rgColor)).getCheckedRadioButtonId();  //заносим в переменную ид отмеченной радиобуттон
        int color = Color.WHITE;                                                                //создаем переменную для передачи цвета текста

        switch (selectColor) {                                                  //в зависимости от нажатой радиобуттон выбираем соответствующий цвет текста
            case R.id.radio1: color = Color.parseColor("#ffffffff");break;      //#ff- первые два знака прозрачность ff 00 00 - цвет. можно задать прозрачность в диапазоне #00-ff например #66- полупрозрачный
            case R.id.radio2: color = Color.parseColor("#aa00ff00");break;      //Полупрозрачный Зеленый цвет
            case R.id.radio3: color = Color.BLUE;break;                         //Синий цвет
        }

        CheckBox checkzeroback=(CheckBox)findViewById(R.id.checkzeroback);      //привязываемся к чекбоксу чекзеробек
        int layoutbackground;                                                   //переменная для выбора бекграунда лайота
        if(checkzeroback.isChecked()) {layoutbackground = R.drawable.backzero;} //в зависимости от состояния чекбокса задаем прозрачный бекграунд если 1
                                else {layoutbackground = R.drawable.back;}      // задаем серый бекграунд если 0

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

        // Записываем значения с экрана в Preferences
        SharedPreferences sp = getSharedPreferences(WIDGET_PREF, MODE_PRIVATE);
        Editor editor = sp.edit();
        editor.putInt(BACK_COLOR + widgetID, layoutbackground);//задаем бекграунд виджета
        editor.putInt(TEXT_COLOR + widgetID, color); //задаем цвет текста виджета
        editor.commit();

/*      //Передаем виджету время и дату - сейчас эти преференсес не использую потому как время и дату задаю напрямую из виджета
        editor.putString(WIDGET_TIME + widgetID, ettime.getText().toString()); //передаем виджету значение из эдиттекста - не использую
        editor.putString(WIDTIME + widgetID, widtime); //!!! именно здесь передаем виджету время !!!
        editor.putString(WIDDATE + widgetID, widdate); //!!! именно здесь передаем виджету дату !!!
*/

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        Timewidget.updateWidget(this, appWidgetManager, sp, widgetID);
        // положительный ответ
        setResult(RESULT_OK, resultValue);
        Log.d(LOG_TAG, "finish config " + widgetID);
        finish();
    }
}
