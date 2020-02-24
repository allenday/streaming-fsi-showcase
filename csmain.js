let genData = [];

// Your web app's Firebase configuration
const firebaseConfig = {
    apiKey: "AIzaSyCRuxL33WHgy3E5zeBMke_8rlhCIrZhl4A",
    authDomain: "ethereum-streaming-dev.firebaseapp.com",
    databaseURL: "https://ethereum-streaming-dev.firebaseio.com",
    projectId: "ethereum-streaming-dev",
    storageBucket: "ethereum-streaming-dev.appspot.com",
    messagingSenderId: "249269731739",
    appId: "1:249269731739:web:97dd7bfaa4991d5f"
};

// Initialize Firebase
firebase.initializeApp(firebaseConfig);
firebase
    .firestore()
    .collection("demo_candlestick")
    .orderBy("timestamp", "desc")
    .limit(30)
    .onSnapshot(querySnapshot => {
        let entries = [];

        querySnapshot.forEach(doc => {
            const { timestamp, candlestick } = doc.data();
            entries.push(
                {
                    open: Number((candlestick.open / 1000000000).toFixed(2)),
                    close: Number((candlestick.close / 1000000000).toFixed(2)),
                    low: Number((candlestick.low / 1000000000).toFixed(2)),
                    high: Number((candlestick.high / 1000000000).toFixed(2)),
                    timestamp: new Date(timestamp)
                }
            );
        });

        entries = entries.sort((a, b) => d3.ascending(a.timestamp, b.timestamp));

        genData = entries;
        displayCS();
        displayInfobar(genData.length - 1);
    });

function displayCS() {
    let chart  = cschart().Bheight(500);
    d3.select("#chart1").call(chart);

    hoverAll();
}

function hoverAll() {
    d3
        .select("#chart1")
        .select(".bands")
        .selectAll("rect")
        .on("mouseover", function (d, i) {
            d3.select(this).classed("hoved", true);
            d3.select(".stick" + i).classed("hoved", true);
            d3.select(".candle" + i).classed("hoved", true);
            d3.select(".volume" + i).classed("hoved", true);
            d3.select(".sigma" + i).classed("hoved", true);
            displayInfobar(i);
        })
        .on("mouseout", function (d, i) {
            d3.select(this).classed("hoved", false);
            d3.select(".stick" + i).classed("hoved", false);
            d3.select(".candle" + i).classed("hoved", false);
            d3.select(".volume" + i).classed("hoved", false);
            d3.select(".sigma" + i).classed("hoved", false);
            displayInfobar(genData.length - 1);
        });
}

function displayInfobar(mark) {
    d3
    .select("#infobar")
    .datum(genData.slice(mark)[0])
    .call(selection => {
        selection.each(({
            timestamp,
            open,
            high,
            low,
            close
        }) => {
            d3.select("#infodate").text(d3.timeFormat("%H:%M")(timestamp));
            d3.select("#infoopen").text(open);
            d3.select("#infohigh").text(high);
            d3.select("#infolow").text(low);
            d3.select("#infoclose").text(close);
        });
    });
}
