const listContainer = document.querySelector('#service-list');
const service_properties = ["name","url","createDate","serverStatus"];
const root_request =new Request('/service');
fetch(root_request)
    .then(function(response) { return response.json(); })
    .then(function(serviceList) {
        serviceList.forEach(service => {

            let service_row = document.createElement("tr");

                service_properties.forEach(property => {
                    let service_property = document.createElement("td");
                    service_property.appendChild(document.createTextNode(service[property]));
                    service_row.appendChild(service_property);
                });
                let delete_button = document.createElement("button");
                delete_button.innerHTML='Delete';
                delete_button.onclick = evt => {
                    fetch('/service', {
                        method: 'delete',
                        headers: {
                            'Accept': 'application/json, text/plain, */*',
                            'Content-Type': 'application/json'
                        },
                        body: JSON.stringify({
                            url: service.url
                        })
                    }).then(res => location.reload()
                );
                }

                service_row.appendChild(delete_button)
        listContainer.appendChild(service_row);
    });
    });

document.querySelector('#post-service').onclick = evt => {
    fetch('/service', {
        method: 'post',
        headers: {
            'Accept': 'application/json, text/plain, */*',
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            url:document.querySelector('#url').value,
            name:document.querySelector('#name').value})
    }).then(res=> location.reload());
}


