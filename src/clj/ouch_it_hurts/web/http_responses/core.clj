(ns ouch-it-hurts.web.http-responses.core)



(defn with-headers [resp headers-map]
  (update-in resp [:headers] merge headers-map))

(defn ok
  ([] (ok nil))
  ([body]
   {:status 200
    :headers {}
    :body body}))


(defn not-modified []
   {:status 304
    :headers {}
    :body ""})

(defn not-found
  ([] (not-found nil))
  ([body]
   {:status 404
    :headers {}
    :body body}))


(defn method-not-allowed
  ([] (method-not-allowed nil))
  ([body]
   {:status 405
    :headers {}
    :body body}))


(defn internal-server-error
  ([] (internal-server-error nil))
  ([body]
   {:status 500
    :headers {}
    :body body}))


(defn json-ok
  ([body] (-> (ok body)
              (with-headers {"Content-type" "application/json"}))))

(defn json [resp])
