package com.rasanenj.warp.chart;

/**
 * @author gilead
 *
 * TODO: the line chart still fluctuates
 */
public class Chart {
    public final static void init() {
        String type = "bar";
        // String type = "line";
        initGraph(60, type);
    }

    public final native static void initGraph(int maxLength, String type) /*-{
        this.maxLength = maxLength;
        var emptyData = function () {
            var values = [];
            return [
                {
                    values: values,      //values - represents the array of {x,y} data points
                    key: 'Data', //key  - the name of the series.
                    color: '#ff7f0e'  //color - optional: choose your own line color.
                }
            ];
        };

        var nv = $wnd.nv;
        var d3 = $wnd.d3;

        nv.addGraph(function() {
            var chart;
            if (type === 'bar') {
                chart = nv.models.historicalBarChart();
            }
            else {
                chart = nv.models.lineChart();
            }

            chart
            .margin({left: 40})  //Adjust chart margins to give the x-axis some breathing room.
            //
            .transitionDuration(350)  //how fast do you want the lines to transition?
            .showLegend(false)       //Show the legend, allowing users to turn on/off line series.
            .showYAxis(true)        //Show the y-axis
            .showXAxis(false)        //Show the x-axis
            .tooltips(false)
            ;

            if (type === 'line') {
                chart.lines.interpolate('linear');
                chart.useInteractiveGuideline(false);
            }

            $wnd.warpChart = chart;

            chart.xAxis     //Chart x-axis settings
                .tickFormat(d3.format(',r'));

            chart.yAxis     //Chart y-axis settings
                .tickFormat(d3.format('.02f'));


            var myData = emptyData();   //You need data...

            $wnd.warpChartData = myData;

            d3.select('#chart svg')    //Select the <svg> element you want to render the chart in.
                    .datum(myData)         //Populate the <svg> element with chart data...
            .call(chart);          //Finally, render the chart!

            //Update the chart when window resizes.
            nv.utils.windowResize(function() { chart.update() });
            return chart;
        });
    }-*/;

    public final native static void addPointToGraph(float value) /*-{
        var _ = $wnd._;

        var chart = $wnd.warpChart;
        var data =  $wnd.warpChartData;
        var values = data[0].values;

        var index = 0;
        var last = _.last(values);
        if (last !== undefined) {
            index = last.x + 1;
        }

        values.push({x: index, y: value});


        if (values.length > this.maxLength) {
            data[0].values.shift();
        }

        chart.update();

    }-*/;
}
