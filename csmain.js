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

const chartDimensions = {
    large: {
        margin: {
            top: 50,
            right: 20,
            bottom: 40,
            left: 50
        },
        width: 1080,
        height: 500
    },
    small: {
        margin: {
            top: 0,
            right: 20,
            bottom: 20,
            left: 50
        },
        width: 500,
        height: 220
    }
}

// Initialize Firebase
firebase.initializeApp(firebaseConfig);

function createChart({
    type,
    size,
    id: containerID,
    infoSelector = '#infobar',
    // height,
    collection,
    limit,
    divider
}) {
    let chartData = [];

    const firestoreCollection = firebase
        .firestore()
        .collection(collection)
        .orderBy("timestamp", "desc")
        .limit(limit || 30);

        firestoreCollection.get().then(onGettingData);
        firestoreCollection.onSnapshot(onGettingData);

    function onGettingData (querySnapshot) {
        let entries = [];

        querySnapshot.forEach(doc => {
            const { timestamp, candlestick } = doc.data();
            let entry;

            if (divider) {
                entry = {
                    open: Number((candlestick.open / divider).toFixed(2)),
                    close: Number((candlestick.close / divider).toFixed(2)),
                    low: Number((candlestick.low / divider).toFixed(2)),
                    high: Number((candlestick.high / divider).toFixed(2)),
                    timestamp: new Date(timestamp)
                }
            } else {
                entry = {
                    open: candlestick.open,
                    close: candlestick.close,
                    low: candlestick.low,
                    high: candlestick.high,
                    timestamp: new Date(timestamp)
                }
            }
            entries.push(entry);
        });

        entries = entries.sort((a, b) => d3.ascending(a.timestamp, b.timestamp));

        chartData = entries;

        const chart = renderChart({
            type,
            size,
            data: chartData
        // }).Bheight(height);
        });

        d3.select(containerID).call(chart);

        d3
            .selectAll(`${containerID} .bands rect`)
            .on("mouseover", function (d, i) {
                d3.select(this).classed("hoved", true);
                d3.select(`${containerID} .stick${i}`).classed("hoved", true);
                d3.select(`${containerID} .candle${i}`).classed("hoved", true);
                d3.select(`${containerID} .volume${i}`).classed("hoved", true);
                d3.select(`${containerID} .sigma${i}`).classed("hoved", true);

                displayInfobar({
                    infoSelector,
                    chartData,
                    mark: i
                });
            })
            .on("mouseout", function (d, i) {
                d3.select(this).classed("hoved", false);
                d3.select(`${containerID} .stick${i}`).classed("hoved", false);
                d3.select(`${containerID} .candle${i}`).classed("hoved", false);
                d3.select(`${containerID} .volume${i}`).classed("hoved", false);
                d3.select(`${containerID} .sigma${i}`).classed("hoved", false);

                displayInfobar({
                    infoSelector,
                    chartData,
                    mark: chartData.length - 1
                });
            });

        displayInfobar({
            infoSelector,
            chartData,
            mark: chartData.length - 1
        });
    }
}

function displayInfobar({infoSelector, chartData, mark}) {
    d3
    .select(infoSelector)
    .datum(chartData.slice(mark)[0])
    .call(selection => {
        selection.each(({
            timestamp,
            open,
            high,
            low,
            close
        }) => {
            d3.select(`${infoSelector} #infodate`).text(d3.timeFormat("%H:%M")(timestamp));
            d3.select(`${infoSelector} #infoopen`).text(open);
            d3.select(`${infoSelector} #infohigh`).text(high);
            d3.select(`${infoSelector} #infolow`).text(low);
            d3.select(`${infoSelector} #infoclose`).text(close);
        });
    });
}

function dimensions(size) {
    return chartDimensions[size];
}

function renderChart({ type, size, data }) {
    if (type !== 'ethereum' && type !== 'trade') {
        return;
    }

    const { margin, width, height } = dimensions(size);
    // let Bheight = 460;
    let Bheight = height;

    function csrender(selection) {
        selection.each(function () {
            const minimal = d3.min(data, d => d.low);
            const maximal = d3.max(data, d => d.high);

            const x = d3.scaleBand().range([0, width]);
            let y;

            if (type === 'ethereum') {
                y = d3.scaleLog()
                    .domain([1, 10000])
                    .range([height, 0])
                    .base(10);
            } else if (type === 'trade') {
                y = d3.scaleLinear()
                    .domain([1, 10000])
                    .range([height, 0]);
            }

            const xAxis = d3.axisBottom().scale(x).tickFormat(d3.timeFormat("%H:%M"));

            x.domain(data.map(d => d.timestamp));
            y.domain([minimal, maximal]).nice();

            const xtickdelta = Math.ceil(60 / (width / data.length));

            xAxis.tickValues(x.domain().filter((d, i) => {
                return !((i + Math.floor(xtickdelta / 2)) % xtickdelta);
            }));

            const barwidth = x.bandwidth();
            const candlewidth = Math.floor(d3.min([barwidth * 0.8, 13]) / 2) * 2 + 1;
            const delta = Math.round((barwidth - candlewidth) / 2);

            d3.select(this).select("svg").remove();

            const svg = d3
                .select(this)
                .append("svg")
                .attr("width", width + margin.left + margin.right)
                .attr("height", Bheight + margin.top + margin.bottom)
                .append("g")
                .attr("transform", "translate(" + margin.left + "," + margin.top + ")");

            svg.append("g")
                .attr("class", "axis xaxis")
                .attr("transform", "translate(0," + height + ")")
                .call(xAxis);

            const yTicksCount = Math.floor(height / 100) < 3 ? 3 : Math.floor(height / 100);
            svg.append("g")
                .attr("class", "axis grid")
                .attr("transform", "translate(" + width + ",0)")
                .call(d3.axisLeft().scale(y).ticks(yTicksCount).tickFormat(d3.format("")).tickSize(width).tickSizeOuter(0));

            const bands = svg
                .selectAll(".bands")
                .data([data])
                .enter()
                .append("g")
                .attr("class", "bands");

            bands.selectAll("rect")
                .data(d => d)
                .enter()
                .append("rect")
                .attr("x", d => x(d.timestamp) + Math.floor(barwidth / 2))
                .attr("y", 0)
                .attr("height", Bheight)
                .attr("width", 1)
                .attr("class", (d, i) => "band" + i)
                .style("stroke-width", Math.floor(barwidth));

            var stick = svg.selectAll(".sticks")
                .data([data])
                .enter()
                .append("g")
                .attr("class", "sticks");

            stick.selectAll("rect")
                .data(d => d)
                .enter()
                .append("rect")
                .attr("x", d => x(d.timestamp) + Math.floor(barwidth / 2))
                .attr("y", d => y(d.high))
                .attr("class", (d, i) => "stick" + i)
                .attr("height", d => y(d.low) - y(d.high))
                .attr("width", 1)
                .classed("rise", d => (d.close > d.open))
                .classed("fall", d => (d.open > d.close));

            var candle = svg.selectAll(".candles")
                .data([data])
                .enter()
                .append("g")
                .attr("class", "candles");

            candle.selectAll("rect")
                .data(d => d)
                .enter()
                .append("rect")
                .attr("x", d => x(d.timestamp) + delta)
                .attr("y", d => y(d3.max([d.open, d.close])))
                .attr("class", (d, i) => "candle" + i)
                .attr("height", d => y(d3.min([d.open, d.close])) - y(d3.max([d.open, d.close])))
                .attr("width", candlewidth)
                .classed("rise", ({ close, open }) => close > open)
                .classed("fall", ({ close, open }) => open > close);

        });
    }

    csrender.Bheight = value => {
        if (!arguments.length) return Bheight;
        Bheight = value;
        return csrender;
    };

    return csrender;
}
