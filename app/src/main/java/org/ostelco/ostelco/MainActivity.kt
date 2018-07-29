package org.ostelco.ostelco

import android.os.AsyncTask
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.util.Base64
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject


class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"
    private var mAuth: FirebaseAuth? = null
    private var token: String = ""
    private var msisdn: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        mAuth = FirebaseAuth.getInstance()

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()

            mAuth!!.signInWithCustomToken(token)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCustomToken:success")
                            storeFirebaseCloudMessageToken(getToken(), msisdn)
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCustomToken:failure", task.exception)
                        }
                    }
        }

        class Auth : AsyncTask<Void, Void, String>() {

            private val TAG = "Auth"

            var client = OkHttpClient()

            override fun doInBackground(vararg params: Void?): String? {


                val URL = "http://10.6.4.67:8080/auth/token"

                val builder = Request.Builder()
                builder.url(URL)
                val request = builder.build()

                try {
                    val response = client.newCall(request).execute()
                    val token = response?.body()?.string()
                    Log.d(TAG, "Response: $token")
                    return token
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to download", e)
                }
                return null
            }

            public override fun onPostExecute(result: String) {
                super.onPostExecute(result)

                val claim = result.split(".")[1]
                val jsonClaim = JSONObject(String(Base64.decode(claim, 0))).getJSONObject("claims")
                Log.d(TAG, "claim: " + jsonClaim.toString())
                msisdn = jsonClaim.get("msisdn").toString()
                Log.d(TAG, "msisdn: $msisdn")

                val txt = findViewById<TextView>(R.id.msisdn)
                txt.text = msisdn

                token = result
            }
        }

        Auth().execute()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun getToken() : String? {
        val fbToken = FirebaseInstanceId.getInstance().token
        Log.d(TAG, "Firebase token: $fbToken")
        return fbToken
    }

    private fun storeFirebaseCloudMessageToken(token: String?, msisdn: String) {
        val mDatabase = FirebaseDatabase.getInstance().reference
        mDatabase.child("firebaseCloudMessage").child(msisdn).setValue(token)
    }
}
