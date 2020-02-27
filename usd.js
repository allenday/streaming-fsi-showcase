firebase
    .firestore()
    .collection("demo_trades")
    .orderBy("timestamp", "desc")
    .limit(30)
    .onSnapshot(querySnapshot => {
        let entries = [];

        querySnapshot.forEach(doc => {
            const { timestamp, candlestick } = doc.data();
            entries.push(
                {
                    open: Number((candlestick.open / 100).toFixed(2)),
                    close: Number((candlestick.close / 100).toFixed(2)),
                    low: Number((candlestick.low / 100).toFixed(2)),
                    high: Number((candlestick.high / 100).toFixed(2)),
                    timestamp: new Date(timestamp)
                }
            );
        });

        entries = entries.sort((a, b) => d3.ascending(a.timestamp, b.timestamp));

        genData = entries;
        displayCS('usd');
        displayInfobar(genData.length - 1);
    });
