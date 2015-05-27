var colors = ["cyan", "red", "yellow", "green", "blue"]; 

function RandomColors() {

    this.get = function () {
        var which = Math.floor(Math.random() * colors.length);
        return colors[which];
    };
 }

window.other = {
    RandomColors: RandomColors
};