es = new EventSource("/events");
es.addEventListener("message", function(ev) {
                                 document.getElementById("rate").innerText = ev.data;
                               });
