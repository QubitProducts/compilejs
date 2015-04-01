//:import other.RandomColors

/*html*/
    <h1 class="pain-class"> Eyes Pain Header</h1>
    <h2> It hurts my eyes.</h2>
    <div id="random"> </div>
/*~html*/

/*css*/
    .pain-class {
        background: #06FF02;
        color: #FFF700;
    }
/*~css*/

var randomColors = new other.RandomColors();

function cycle () {
    var randomNumber = Math.floor(Math.random() * 999);
    var node = document.getElementById("random");
    node.innerHTML = randomNumber;
    node.style.backgroundColor = randomColors.get();
    setTimeout(cycle, 130);
}

cycle();