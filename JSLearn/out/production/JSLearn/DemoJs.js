/**
 *
 * Created by user on 2016/3/19.
 */

//HF.Math.RandomNumbers = function (startNum, endNum, count, repeat) {
function s(arr){
    var sum = function() {
        return arr.reduce(function(x, y) {
            return x + y;
        });
    };
    return sum;
}
var x = [11, 23, 12, 2, 4, 22, 14, 44];
var f = s(x);

