(ns ouch-it-hurts.web.http-responses.core)

(defn with-headers [resp headers-map]
  (update-in resp [:headers] merge headers-map))

(defn ok
  ([] (ok nil))
  ([body]
   {:status 200
    :headers {}
    :body body}))

(defn created
  ([] (created nil))
  ([body]
   {:status 201
    :headers {}
    :body body}))

(defn bad-request
  ([]  (bad-request nil))
  ([body]
   {:status 400
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

(defn response-as-json
  [response]
  (with-headers response {"content-type" "application/json"}))

(defn json-created
  ([] (created nil))
  ([body] (-> (created body)
              (response-as-json))))

(defn json-ok
  ([] (json-ok nil))
  ([body] (-> (ok body)
              (response-as-json))))

(defn json-bad-request
  ([] (json-bad-request nil))
  ([body] (-> (bad-request body)
              (response-as-json))))

(defn page [body]
  (-> (ok body)
      (with-headers {"content-type" "text/html"})))
