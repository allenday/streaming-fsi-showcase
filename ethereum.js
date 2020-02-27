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
        displayCS('ethereum');
        displayInfobar(genData.length - 1);
    });
