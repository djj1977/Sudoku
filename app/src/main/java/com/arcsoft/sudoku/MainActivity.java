package com.arcsoft.sudoku;

import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    SudokuElement[][] sudokuArray = null;
    TextView[][] viewList = new TextView[9][9];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initSudoku();
        initElementView();
        process();
    }

    public void filter_row_col_9grid()
    {
        boolean cleared = false;
        do {
            filter_9grid();
            cleared = filter_by_new_calculated_value(); //再次过滤
            //filter_row();
            //cleared = filter_by_new_calculated_value(); //再次过滤
            //filter_column();

        }while (cleared);
    }

    public void process() {

        boolean continueFilter = false;
        //filter1： 余数法，计算每个空格的初始取值范围
        //一格受其所在单元中其他20格的牵制（行，列，9宫格，假如这20格里面已经出现了1-8这8个数字，我们就可以断定这格一定是未出现的唯一数字9
        filer_all_row_colum_9grid();

        do {
            continueFilter  = false;
            //filter2：根据某个空格新算出来数值，对其所在的行，列，九宫格内其他空格做取值范围缩减
            if (true ==filter_by_new_calculated_value())
                continueFilter = true;

            //filter3：宫摒除法， 某个可能的取值数值只在9宫格中某个空格中有（不能有两个空格都显示这个可能的取值数值）
            if (true == filter_9grid())
                continueFilter = true;

            if (true == filter_by_new_calculated_value())//再次过滤
                continueFilter = true;

            //filter4: 行摒除法， 某个可能的取值数在某行中唯一存在， 则对应的空格就是这个数值
            if (true == filter_row())
                continueFilter = true;

            if (true == filter_by_new_calculated_value()) //再次过滤
                continueFilter = true;

            //filter5: 列摒除法，某个可能的取值数在某列中唯一存在， 则对应的空格就是这个数值
            if (true == filter_column())
                continueFilter = true;

            if (true == filter_by_new_calculated_value()) //再次过滤
                continueFilter = true;

            //filter： x-wing
            if (true == filter_by_x_wing())
                continueFilter = true;

            if (true == filter_by_new_calculated_value())
                continueFilter = true;

        }while (continueFilter);

        //filter_row_col_9grid();

        printInterResult();


    }

    public boolean filter_column()
    {
        boolean flag = false;
        for (int j = 0; j < 9; j ++)
            _filter_only_value_on_col(j);

        return flag;
    }

    public boolean filter_row()
    {
        boolean flag = false;
        for (int i = 0; i < 9; i ++)
            _filter_only_value_on_row(i);

        return flag;
    }

    public boolean filter_9grid()
    {
        boolean flag = false;
        for (int i = 0; i < 3; i++)
            for (int j = 0; j < 3; j++)
                if (true == _filter_only_value_on_9gid(i, j)){//第i行，第j个 9宫格
                    flag = true;
                }

        return flag;
    }

    class LineStatitic{
        NumStatistic[] numArray = null;
        public LineStatitic()
        {
            numArray = new NumStatistic[9];
            for (int i = 0; i < 9; i ++)
                numArray[i] = new NumStatistic(i+1);
        }

        public void addVal(int val, int row, int col)
        {
            numArray[val-1].addVal(val, row, col); //array index from 0 to 8, while value from 1 to 9
        }
    }
    class NumStatistic
    {
        int value; //from 1 to 9
        ArrayList<Integer> list = null; //这个数值分布在哪些空格
        public NumStatistic(int val)
        {
            value = val;
            list = new ArrayList<Integer>();
        }

        public void addVal(int val, int row, int col)
        {
            list.add(col);
        }
    }

    class Blank{
        int row;
        int col;
    }

    public boolean filter_by_x_wing()
    {
        boolean flag = false;
        //关于x-wing 的解释， 查看下面网页的说明
        //http://www.sudokufans.org.cn/forums/topic/8/


        LineStatitic[] lineArray = new LineStatitic[9]; //9行
        for (int i = 0; i < 9; i ++)
            lineArray[i] = new LineStatitic();

        //统计每行每个取值数字的分布
        for (int i = 0; i < 9; i ++)
        {
            for (int j = 0; j < 9; j++) {
                SudokuElement element = sudokuArray[i][j];
                if (element.getValue() > 0) continue;

                ArrayList<Integer> rangeList = element.getValueRange();
                for (Integer val: rangeList)
                    lineArray[i].addVal(val, i, j);
            }
        }


        //对每一个数字check 是否存在x-wing
        for (int val = 0; val < 9; val ++)
        {

            for (int lineindex = 0; lineindex < 9; lineindex ++) {
                Blank lt, rt, lb,rb; //lefttop, righttop, leftbottom, rightbottom
                int left=0, right=0;
                int leftbottom = 0, rightbottom = 0;
                int leftcount = 0;
                int rightcount = 0;
                int paricount = 0;

                lt = new Blank();
                rt = new Blank();
                lb = new Blank();
                rb = new Blank();

                if (lineArray[lineindex].numArray[val].list.size() != 2)
                    continue;
                else {
                        left = lineArray[lineindex].numArray[val].list.get(0);
                        right = lineArray[lineindex].numArray[val].list.get(1);

                        lt.row = lineindex;
                        lt.col = left;
                        rt.row = lineindex;
                        rt.col = right;
                }
                for (int otherline = lineindex+1; otherline < 9; otherline ++)
                {
                    if (lineArray[otherline].numArray[val].list.size() != 2)
                        continue;

//                    if (lineArray[otherline].numArray[val].list.get(0) == left) leftcount ++;
//                    if (lineArray[otherline].numArray[val].list.get(1) == right) rightcount ++;

                    if ((lineArray[otherline].numArray[val].list.get(0) == left) &&
                            (lineArray[otherline].numArray[val].list.get(1) == right)) {
                        lb.row = otherline;
                        lb.col = left;
                        rb.row = otherline;
                        rb.col = right;
                        paricount++;
                    }
                }

//                if (leftcount == 1 && rightcount == 1 && paricount == 1) //find x-wing
                if (paricount == 1) //find x-wing
                {
                    flag = true;
                    //left 和 right 所在列 除了数字所在行， 其他空格的取值范围都不能取这个数字

                    for (int rowindex = 0; rowindex < 9; rowindex ++)
                    {
                        if (rowindex == lt.row) continue;
                        if (rowindex == lb.row) continue;

                        if(sudokuArray[rowindex][lt.col].getValue() > 0 &&
                                sudokuArray[rowindex][rt.col].getValue() > 0) //已有数值或者已经算出的数值忽略，
                            continue;

                        ArrayList<Integer> rangeList = null;
                        //去除第lt.col 列的x-wing找的的数字
                        rangeList =  sudokuArray[rowindex][lt.col].getValueRange();
                        for (int k = rangeList.size()-1; k >= 0; k --)
                        {
                            if ((val+1) == rangeList.get(k)) //val +1 是实际数字
                                rangeList.remove(k);
                        }
                        if (rangeList.size() == 1) {
                            sudokuArray[rowindex][lt.col].setValue(rangeList.get(0));
                            sudokuArray[rowindex][lt.col].calculated = true; //触发重计算
                        }

                        //去除第rt.col 列的x-wing找的的数字
                        rangeList =  sudokuArray[rowindex][rt.col].getValueRange();
                        for (int k = rangeList.size()-1; k >= 0; k --)
                        {
                            if ((val+1) == rangeList.get(k)) //val +1 是实际数字
                                rangeList.remove(k);
                        }
                        if (rangeList.size() == 1) {
                            sudokuArray[rowindex][rt.col].setValue(rangeList.get(0));
                            sudokuArray[rowindex][rt.col].calculated = true; //触发重计算
                        }


                    }


                    //filter_by_new_calculated_value();
                }

            }
        }
        return flag;
    }

    public boolean _filter_only_value_on_row(int row)
    {
        boolean flag = false;
        int count = 0;
        int index_col = 0;

        for (int k = 1; k <=9; k ++) {
            index_col = 0;
            count  = 0;

            for (int j = 0; j < 9; j++) {
                if (sudokuArray[row][j].getValue() > 0) continue;

                ArrayList<Integer> rangeList = sudokuArray[row][j].getValueRange();
                //if (rangeList.size() <= 0) continue;

                for (Integer val : rangeList) {
                    if (val.equals(k)) //取值范围内有包含K 这个数值
                    {
                        index_col = j;
                        count++;
                    }
                }
            }

            if (1 == count) //在这行中的每个空格的取值范围中仅有这个数值是唯一
            {
                //这个空格的真实值就是这个数值
                sudokuArray[row][index_col].setValue(k);
                sudokuArray[row][index_col].calculated = true;
                flag = true;
            }
        }

        return flag;
    }

    public boolean _filter_only_value_on_col(int col)
    {
        int count = 0;
        int index_row = 0, index_col = 0;
        boolean flag = false;

        for (int k = 1; k <=9; k ++) {
            index_col = 0;
            count = 0;

            for (int i = 0; i < 9; i++) {
                if (sudokuArray[i][col].getValue() > 0) continue;

                ArrayList<Integer> rangeList = sudokuArray[i][col].getValueRange();
                if (rangeList.size() <= 0) continue;

                for (Integer val : rangeList) {
                    if (val.equals(k)) //取值范围内有包含K 这个数值
                    {
                        index_row = i;
                        count++;
                    }
                }
            }
            if (1 == count) //在该列的每个空格的取值范围中仅有这个数值是唯一
            {
                //这个空格的真实值就是这个数值
                sudokuArray[index_row][col].setValue(k);
                sudokuArray[index_row][col].calculated = true;
                flag = true;
            }
        }

        return flag;
    }

    public void filer_all_row_colum_9grid()
    {
        ArrayList<Integer> rangeList = null;

        for (int i = 0; i < 9; i ++)
            for (int j = 0; j < 9; j ++)
            {
                if (sudokuArray[i][j].getValue() > 0) //已经有初始值或者算出数值就不用再计算取值范围了
                    continue;
                //计算当前行可能的取值范围
                rangeList = sudokuArray[i][j].getValueRange();
                for (int val = 1; val <=9; val ++)
                {
                    if (isValueIn9Grid(val, i, j)) continue;
                    if (isValueInColumn(val, j)) continue;
                    if (isValueInRow(val, i)) continue;
                    if (isValueInList(val, sudokuArray[i][j])) continue;
                    rangeList.add(val);
                }

                if (rangeList.size() == 1)//如果取值范围只有1个， 则这个就是算出来的数值，设置value
                {
                    int val = sudokuArray[i][j].getValueRange().get(0);
                    //设置该空格的数值
                    sudokuArray[i][j].setValue(val);
                    sudokuArray[i][j].calculated = true;
                }
            }
    }

    public boolean filter_by_new_calculated_value()
    {
        boolean calculated = false; //有计算出数值
        boolean cleared = false;
        //do {
            calculated = false;
            for (int i = 0; i <9; i ++)
                for (int j = 0; j < 9; j ++)
                {
                    if (sudokuArray[i][j].calculated) //在各种filter下也可能算出真实数值
                    {
                        calculated = true;
                        int val = sudokuArray[i][j].getValue();
                        //对应行，列，9宫格 的空格的取值范围去除这个数值
                        clearPossibleValue(i, j, val);
                        sudokuArray[i][j].calculated = false; //已经被使用过了， 就当成已存在的数值
                        cleared = true;
                    }
                }
        //}while(calculated);

        return cleared;
    }


    public boolean _filter_only_value_on_9gid(int gridrow, int gridcol)
    {
        int count = 0;
        int index_row = 0, index_col = 0;
        boolean flag= false;

        for (int k = 1; k <=9; k ++) {
            index_row = 0;
            index_col = 0;
            count  = 0;

            for (int i = gridrow * 3; i < gridrow * 3 + 3; i++)
                for (int j = gridcol * 3; j < gridcol * 3 + 3; j++) {
                    if (sudokuArray[i][j].getValue() > 0) continue;
                    ArrayList<Integer> rangeList = sudokuArray[i][j].getValueRange();
                    if (rangeList.size() <= 0) continue;
                    for(Integer val: rangeList)
                        if (val.equals(k)) //取值范围内有包含K 这个数值
                        {
                            index_row = i;
                            index_col = j;
                            count ++;
                        }
                }

                if (1 == count) //在9宫格的每个空格的取值范围中仅有这个数值是唯一
                {
                    //这个空格的真实值就是这个数值
                    sudokuArray[index_row][index_col].setValue(k);
                    sudokuArray[index_row][index_col].calculated = true;
                    flag = true;
                }
        }

        return flag;
    }

    public void clearPossibleValue(int row, int col, int val)
    {
        //clear row
        for (int i = 0; i < 9; i ++)
        {
            SudokuElement element = sudokuArray[row][i];
            if (element.getValue() > 0) continue;

            ArrayList<Integer> rangeList = element.getValueRange();
            int num = rangeList.size();
            for (int index = num-1; index >= 0; index --)
            {
                if (rangeList.get(index).equals(val))
                    rangeList.remove(index);
            }
            if (rangeList.size() == 1) //only one, means calculated
            {
                element.calculated = true;
                element.setValue(rangeList.get(0));

                clearPossibleValue(row, i, rangeList.get(0));
            }
        }

        //clear column
        for (int i = 0; i < 9; i ++)
        {
            SudokuElement element = sudokuArray[i][col];
            if (element.getValue() > 0) continue;

            ArrayList<Integer> rangeList = element.getValueRange();
            int num = rangeList.size();
            for (int index = num-1; index >= 0; index --)
            {
                if (rangeList.get(index).equals(val))
                    rangeList.remove(index);
            }

            if (rangeList.size() == 1) //only one, means calculated
            {
                element.calculated = true;
                element.setValue(rangeList.get(0));

                clearPossibleValue(i, col, rangeList.get(0));
            }
        }

        //clear 9宫格
        for (int i = row/3 * 3; i < row/3*3 + 3; i ++ )
            for (int j = col/3*3; j < col/3*3 + 3; j ++)
            {
                SudokuElement element = sudokuArray[i][j];
                if (element.getValue() > 0) continue;

                ArrayList<Integer> rangeList = element.getValueRange();
                int num = rangeList.size();
                for (int index = num-1; index >= 0; index --)
                {
                    if (rangeList.get(index).equals(val))
                        rangeList.remove(index);
                }

                if (rangeList.size() == 1) //only one, means calculated
                {
                    element.calculated = true;
                    element.setValue(rangeList.get(0));

                    clearPossibleValue(i, j, rangeList.get(0));
                }
            }
    }

    //打印结果
    public void printInterResult()
    {
        for (int i = 0; i < 9; i ++)
            for (int j = 0; j < 9; j ++) {
                if (sudokuArray[i][j].getValue() > 0 ) {
                    if (sudokuArray[i][j].hasOrignalVal) //初始已经填好
                        continue;//不用打印，已经算出
                    else {
                        int calVal = sudokuArray[i][j].getValue();

                        //调整默认字体大小
                        viewList[i][j].setTextSize(15);
                        viewList[i][j].setTextColor(Color.BLUE);
                        viewList[i][j].setText(Integer.toString(calVal));
                    }
                }
                else {
                    ArrayList<Integer> rangeList = sudokuArray[i][j].getValueRange();
                    int num = rangeList.size();
                    if (num <= 0) continue;
                    String range = "";
                    for (int k = 0; k < num; k++)
                        range += Integer.toString(rangeList.get(k));
                    viewList[i][j].setText(range);
                }
            }
    }

    boolean isValueInList(int val, SudokuElement element)
    {
        int num = element.getValueRange().size();
        if(num <= 0 ) return false;

        ArrayList rangeList = element.getValueRange();
        for (int i = 0; i < num; i ++)
            if (rangeList.get(i).equals(val))
                return true;

        return false;
    }

    boolean isValueInRow(int value, int row)
    {
        for (int i = 0 ; i < 9; i ++)
            if (sudokuArray[row][i].getValue() == value)
                return true;
        return false;
    }

    boolean isValueInColumn(int value, int column)
    {
        for (int i = 0 ; i < 9; i ++)
            if (sudokuArray[i][column].getValue() == value)
                return true;
        return false;
    }

    boolean isValueIn9Grid(int value, int row, int column)
    {
        //先计算出给点的空格在那个9宫格

        int startrow ;
        int startcol;

        startrow = row / 3 * 3;
        startcol = column / 3 * 3;

        for (int i = startrow; i < startrow+3; i ++)
            for (int j = startcol; j < startcol+3; j++)
            {
                if (value == sudokuArray[i][j].getValue())
                    return true;
            }

        return false;
    }


    public void initSudoku()
    {
        sudokuArray = new SudokuElement[9][9];
        int[][] initValueArr = {
//                {0, 5, 0, 0, 0, 0, 0, 2, 0},
//                {4, 0, 0, 2, 0, 6, 0, 0, 7},
//                {0, 0, 8, 0, 3, 0, 1, 0, 0},
//                {0, 1, 0, 0, 0, 0, 0, 6, 0},
//                {0, 0, 9, 0, 0, 0, 5, 0, 0},
//                {0, 7, 0, 0, 0, 0, 0, 9, 0},
//                {0, 0, 5, 0, 8, 0, 3, 0, 0},
//                {7, 0, 0, 9, 0, 1, 0, 0, 4},
//                {0, 2, 0, 0, 0, 0, 0, 7, 0}
                {3, 0, 0, 0, 9, 4, 0, 1, 0},
                {5, 8, 0, 0, 0, 0, 0, 4, 0},
                {0, 0, 0, 3, 0, 0, 0, 0, 6},
                {2, 5, 0, 0, 8, 0, 0, 0, 0},
                {9, 0, 0, 0, 7, 0, 0, 0, 1},
                {0, 0, 0, 0, 4, 0, 0, 8, 9},
                {4, 0, 0, 0, 0, 9, 0, 0, 0},
                {0, 1, 0, 0, 0, 0, 0, 6, 3},
                {0, 9, 0, 7, 1, 0, 0, 0, 2}

        };
        for (int i = 0; i < 9; i ++)
            for (int j = 0; j < 9; j ++)
            {
                boolean flag = (initValueArr[i][j] > 0) ? true: false ;

                sudokuArray[i][j] = new SudokuElement(initValueArr[i][j], flag);
            }
    }

    public void initElementView()
    {

        String viewId;
        viewList[0][0] = (TextView)findViewById(R.id.textView00);
        viewList[0][1] = (TextView)findViewById(R.id.textView01);
        viewList[0][2] = (TextView)findViewById(R.id.textView02);
        viewList[0][3] = (TextView)findViewById(R.id.textView03);
        viewList[0][4] = (TextView)findViewById(R.id.textView04);
        viewList[0][5] = (TextView)findViewById(R.id.textView05);
        viewList[0][6] = (TextView)findViewById(R.id.textView06);
        viewList[0][7] = (TextView)findViewById(R.id.textView07);
        viewList[0][8] = (TextView)findViewById(R.id.textView08);

        viewList[1][0] = (TextView)findViewById(R.id.textView10);
        viewList[1][1] = (TextView)findViewById(R.id.textView11);
        viewList[1][2] = (TextView)findViewById(R.id.textView12);
        viewList[1][3] = (TextView)findViewById(R.id.textView13);
        viewList[1][4] = (TextView)findViewById(R.id.textView14);
        viewList[1][5] = (TextView)findViewById(R.id.textView15);
        viewList[1][6] = (TextView)findViewById(R.id.textView16);
        viewList[1][7] = (TextView)findViewById(R.id.textView17);
        viewList[1][8] = (TextView)findViewById(R.id.textView18);

        viewList[2][0] = (TextView)findViewById(R.id.textView20);
        viewList[2][1] = (TextView)findViewById(R.id.textView21);
        viewList[2][2] = (TextView)findViewById(R.id.textView22);
        viewList[2][3] = (TextView)findViewById(R.id.textView23);
        viewList[2][4] = (TextView)findViewById(R.id.textView24);
        viewList[2][5] = (TextView)findViewById(R.id.textView25);
        viewList[2][6] = (TextView)findViewById(R.id.textView26);
        viewList[2][7] = (TextView)findViewById(R.id.textView27);
        viewList[2][8] = (TextView)findViewById(R.id.textView28);

        viewList[3][0] = (TextView)findViewById(R.id.textView30);
        viewList[3][1] = (TextView)findViewById(R.id.textView31);
        viewList[3][2] = (TextView)findViewById(R.id.textView32);
        viewList[3][3] = (TextView)findViewById(R.id.textView33);
        viewList[3][4] = (TextView)findViewById(R.id.textView34);
        viewList[3][5] = (TextView)findViewById(R.id.textView35);
        viewList[3][6] = (TextView)findViewById(R.id.textView36);
        viewList[3][7] = (TextView)findViewById(R.id.textView37);
        viewList[3][8] = (TextView)findViewById(R.id.textView38);

        viewList[4][0] = (TextView)findViewById(R.id.textView40);
        viewList[4][1] = (TextView)findViewById(R.id.textView41);
        viewList[4][2] = (TextView)findViewById(R.id.textView42);
        viewList[4][3] = (TextView)findViewById(R.id.textView43);
        viewList[4][4] = (TextView)findViewById(R.id.textView44);
        viewList[4][5] = (TextView)findViewById(R.id.textView45);
        viewList[4][6] = (TextView)findViewById(R.id.textView46);
        viewList[4][7] = (TextView)findViewById(R.id.textView47);
        viewList[4][8] = (TextView)findViewById(R.id.textView48);

        viewList[5][0] = (TextView)findViewById(R.id.textView50);
        viewList[5][1] = (TextView)findViewById(R.id.textView51);
        viewList[5][2] = (TextView)findViewById(R.id.textView52);
        viewList[5][3] = (TextView)findViewById(R.id.textView53);
        viewList[5][4] = (TextView)findViewById(R.id.textView54);
        viewList[5][5] = (TextView)findViewById(R.id.textView55);
        viewList[5][6] = (TextView)findViewById(R.id.textView56);
        viewList[5][7] = (TextView)findViewById(R.id.textView57);
        viewList[5][8] = (TextView)findViewById(R.id.textView58);

        viewList[6][0] = (TextView)findViewById(R.id.textView60);
        viewList[6][1] = (TextView)findViewById(R.id.textView61);
        viewList[6][2] = (TextView)findViewById(R.id.textView62);
        viewList[6][3] = (TextView)findViewById(R.id.textView63);
        viewList[6][4] = (TextView)findViewById(R.id.textView64);
        viewList[6][5] = (TextView)findViewById(R.id.textView65);
        viewList[6][6] = (TextView)findViewById(R.id.textView66);
        viewList[6][7] = (TextView)findViewById(R.id.textView67);
        viewList[6][8] = (TextView)findViewById(R.id.textView68);

        viewList[7][0] = (TextView)findViewById(R.id.textView70);
        viewList[7][1] = (TextView)findViewById(R.id.textView71);
        viewList[7][2] = (TextView)findViewById(R.id.textView72);
        viewList[7][3] = (TextView)findViewById(R.id.textView73);
        viewList[7][4] = (TextView)findViewById(R.id.textView74);
        viewList[7][5] = (TextView)findViewById(R.id.textView75);
        viewList[7][6] = (TextView)findViewById(R.id.textView76);
        viewList[7][7] = (TextView)findViewById(R.id.textView77);
        viewList[7][8] = (TextView)findViewById(R.id.textView78);

        viewList[8][0] = (TextView)findViewById(R.id.textView80);
        viewList[8][1] = (TextView)findViewById(R.id.textView81);
        viewList[8][2] = (TextView)findViewById(R.id.textView82);
        viewList[8][3] = (TextView)findViewById(R.id.textView83);
        viewList[8][4] = (TextView)findViewById(R.id.textView84);
        viewList[8][5] = (TextView)findViewById(R.id.textView85);
        viewList[8][6] = (TextView)findViewById(R.id.textView86);
        viewList[8][7] = (TextView)findViewById(R.id.textView87);
        viewList[8][8] = (TextView)findViewById(R.id.textView88);

        for (int i = 0; i < 9; i ++)
            for (int j = 0; j < 9; j ++)
            {

                //调整view背景色
                viewList[i][j].setBackgroundColor(0xffafafaf);

                //调整view 大小
                ViewGroup.LayoutParams p=viewList[i][j].getLayoutParams();
                p.height = 90;
                p.width = 90;
                viewList[i][j].setLayoutParams(p);

                //调整默认字体大小
                viewList[i][j].setTextSize(10);

                //调整数值居中显示
                viewList[i][j].setGravity(Gravity.CENTER);
                //填入初始数值
                if (sudokuArray[i][j].getValue() > 0) {
                    viewList[i][j].setText(Integer.toString(sudokuArray[i][j].getValue()));
                    viewList[i][j].setTextSize(15);
                    viewList[i][j].setTextColor(Color.RED);
                }

            }

    }
}
