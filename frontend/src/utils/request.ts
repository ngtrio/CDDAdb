export function request(url: string, method: string, body?: string): Promise<any> {
    let init: RequestInit = {
        method: method,
        body: body,
        mode: "cors"
    }
    return fetch(url, init).then(res => {
        let t = res.json()
        console.log("get: " + t)
        return t
    })
}