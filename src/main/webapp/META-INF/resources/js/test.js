
$(document).ready(function() {
    $("#pause").hide();
    $("#play").click(function() {
        $("#play").hide();
        $("#pause").show();
    });

    $("#pause").click(function() {
        $("#pause").hide();
        $("#play").show();

    });

    var category_names = [
        "Järjestelyä",
        "Ohjeiden anto",
        "Tehtävien suoritusasdfasdfasdfasdfasdfasdf",
        "Palautteen anto",
        "Lopettelu",
        "Dummy data1",
        "Dummy data2",
        "Dummy data3",
        "Dummy data4"
    ];

    var categories = [];

    function updateTimer(index) {
        var time = categories[index].timer;
        var div = categories[index].timer_div;
        var m = Math.floor(time / 60);
        var sec = time - m * 60;
        //var str = min + ":" + sec;



        if (m < 10) {
            str = "0" + m + ":";
        } else {
            str = m + ":";
        }
        if (sec < 10) {
            str = str + "0" + sec;
        } else {
            str = str + sec;
        }

        div.empty();
        div.append(document.createTextNode(str));
    }

    function createCategoryItem(index) {
        var category = category_names[index];
        var li = $(document.createElement("li"));
        li.addClass("category-item");
        li.attr("id", "category-item_" + index);
        var div = $(document.createElement("div"));
        div.addClass("timer");
        div.append(document.createTextNode("00:26"));
        li.append(div);
        li.append(document.createTextNode(category));
        categories.push({
            down: false,
            timer: 0,
            timer_div: div
        });
        updateTimer(index);
        return li;
    }

    for (var i in category_names) {
        $("#category-list").append(createCategoryItem(i));
    }

    function categoryClick() {
        var elem = $(this);
        var id = elem.attr("id").split("_");
        var index = parseInt(id[1]);
        var category = categories[index];

        if (category.down) {
            elem.removeClass("down");
            category.down = false;
        } else {
            elem.addClass("down");
            category.down = true;
        }
    }

    $(".category-item").click(categoryClick);

    function tick() {
        for (var i in categories) {
            var category = categories[i];
            if (category.down) {
                category.timer++;
                updateTimer(i);
            }
        }
    }

    setInterval(tick, 1000);
});
