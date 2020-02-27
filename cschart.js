function cschart(chartType) {
    if (chartType !== 'ethereum' && chartType !== 'usd') {
        return;
    }

    const margin = {
        top: 50,
        right: 20,
        bottom: 40,
        left: 50
    };
    const width = 1080;
    const height = 500;
    let Bheight = 460;

    function csrender(selection) {
      selection.each(function () {
        const minimal  = d3.min(genData, d => d.low);
        const maximal  = d3.max(genData, d => d.high);

        const x = d3.scaleBand().range([0, width]);
        let y;

if (chartType === 'ethereum') {
            y = d3.scaleLog()
            .domain([1, 10000])
            .range([height, 0])
            .base(10);
        } else if(chartType === 'usd') {
            y = d3.scaleLinear()
            .domain([1, 10000])
            .range([height, 0]);
        }

        const xAxis = d3.axisBottom().scale(x).tickFormat(d3.timeFormat("%H:%M"));

        x.domain(genData.map(d => d.timestamp));
        y.domain([minimal, maximal]).nice();

        const xtickdelta = Math.ceil(60 / (width / genData.length));

        xAxis.tickValues(x.domain().filter((d, i) => {
            return !((i + Math.floor(xtickdelta / 2)) % xtickdelta);
        }));

        const barwidth = x.bandwidth();
        const candlewidth = Math.floor(d3.min([barwidth*0.8, 13]) / 2) * 2 + 1;
        const delta = Math.round((barwidth-candlewidth) / 2);

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

        svg.append("g")
            .attr("class", "axis grid")
            .attr("transform", "translate(" + width + ",0)")
            .call(d3.axisLeft().scale(y).ticks(Math.floor(height / 100)).tickFormat(d3.format("")).tickSize(width).tickSizeOuter(0));

        const bands = svg
            .selectAll(".bands")
            .data([genData])
            .enter()
            .append("g")
            .attr("class", "bands");

        bands.selectAll("rect")
            .data(d => d)
            .enter()
            .append("rect")
            .attr("x", d => x(d.timestamp) + Math.floor(barwidth/2))
            .attr("y", 0)
            .attr("height", Bheight)
            .attr("width", 1)
            .attr("class", (d, i) => "band" + i)
            .style("stroke-width", Math.floor(barwidth));

        var stick = svg.selectAll(".sticks")
            .data([genData])
            .enter()
            .append("g")
            .attr("class", "sticks");

        stick.selectAll("rect")
            .data(d => d)
            .enter()
            .append("rect")
            .attr("x", d => x(d.timestamp) + Math.floor(barwidth/2))
            .attr("y", d => y(d.high))
            .attr("class", (d, i) => "stick" + i)
            .attr("height", d => y(d.low) - y(d.high))
            .attr("width", 1)
            .classed("rise", d => (d.close>d.open))
            .classed("fall", d => (d.open>d.close));

        var candle = svg.selectAll(".candles")
            .data([genData])
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
            .classed("rise", ({close, open}) => close > open)
            .classed("fall", ({close, open}) => open > close);

      });
    }

    csrender.Bheight = function (value) {
        if (!arguments.length) return Bheight;
        Bheight = value;
        return csrender;
    };

    return csrender;
}
