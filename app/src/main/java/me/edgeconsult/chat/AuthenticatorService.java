package me.edgeconsult.chat;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by yun on 10/13/17.
 */

public class AuthenticatorService extends Service {

    private static final String AUTHENTICATOR_SERVICE_TAG = MainActivity.class.getSimpleName();
    private static AccountAuthenticator accountAuthenticator = null;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        IBinder ret = null;
        if (intent.getAction().equals(android.accounts.AccountManager.ACTION_AUTHENTICATOR_INTENT)) {
            ret = getAuthenticator().getIBinder();
        }
        return ret;
    }

    private AccountAuthenticator getAuthenticator() {
        if (AuthenticatorService.accountAuthenticator == null) {
            AuthenticatorService.accountAuthenticator = new AccountAuthenticator(this);
        }

        return AuthenticatorService.accountAuthenticator;
    }

    public class AccountAuthenticator extends AbstractAccountAuthenticator {

        private Context context;

        public AccountAuthenticator(Context context) {
            super(context);
            this.context = context;
        }

        @Override
        public String getAuthTokenLabel(String s) {
            return null;
        }

        @Override
        public Bundle editProperties(AccountAuthenticatorResponse accountAuthenticatorResponse, String s) {
            return null;
        }

        @Override
        public Bundle getAuthToken(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String s, Bundle bundle) throws NetworkErrorException {
            final AccountManager am = AccountManager.get(context);
            String authToken = am.peekAuthToken(account, s);
            //if (TextUtils.isEmpty(authToken)) {
            //  final String password = am.getPassword(account);
            //  if (password != null) {
            //      authToken = sServerAuthenticate.userSignIn(account.name, password, authTokenType);
            //  }
            //}
            if (!authToken.isEmpty()) {
                final Bundle result = new Bundle();
                result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
                result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
                result.putString(AccountManager.KEY_AUTHTOKEN, authToken);
                return result;
            }
            final Intent intent = new Intent(context, AuthenticatorActivity.class);
            intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, accountAuthenticatorResponse);
            intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, account.type);
            final Bundle b = new Bundle();
            b.putParcelable(AccountManager.KEY_INTENT, intent);
            return b;
        }

        @Override
        public Bundle addAccount(AccountAuthenticatorResponse accountAuthenticatorResponse, String s, String s1, String[] strings, Bundle bundle) throws NetworkErrorException {
            AccountManager accountManager = AccountManager.get(context);
            if (accountManager.getAccountsByType(getString(R.string.account_type)).length == 0) {
                final Intent intent = new Intent(context, AuthenticatorActivity.class);
                intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, accountAuthenticatorResponse);
                intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, s);
                final Bundle b = new Bundle();
                b.putParcelable(AccountManager.KEY_INTENT, intent);
                return b;
            } else {
                final Bundle b = new Bundle();
                b.putInt(AccountManager.KEY_ERROR_CODE, 1);
                b.putString(AccountManager.KEY_ERROR_MESSAGE, "Only one account allowed");
                return b;
            }
        }

        @Override
        public Bundle confirmCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, Bundle bundle) throws NetworkErrorException {
            return null;
        }

        @Override
        public Bundle updateCredentials(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String s, Bundle bundle) throws NetworkErrorException {
            return null;
        }

        @Override
        public Bundle hasFeatures(AccountAuthenticatorResponse accountAuthenticatorResponse, Account account, String[] strings) throws NetworkErrorException {
            return null;
        }
    }
}
