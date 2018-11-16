package com.tn.web.service.coin;

import client.EosApiClientFactory;
import client.EosApiRestClient;
import client.domain.common.transaction.PackedTransaction;
import client.domain.common.transaction.SignedPackedTransaction;
import client.domain.common.transaction.TransactionAction;
import client.domain.common.transaction.TransactionAuthorization;
import client.domain.response.chain.AbiJsonToBin;
import client.domain.response.chain.Block;
import client.domain.response.chain.account.Account;
import client.domain.response.chain.transaction.PushedTransaction;
import javafx.util.converter.BigDecimalStringConverter;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Rpctest {
    private EosApiRestClient thisClient;
    private String walletUrl = "http://127.0.0.1:55553";
    private String chainUrl = "http://10.214.199.111:8888";
    private String historyUrl = "http://10.214.199.111:8888";

    private String ownerAccount = "jrlee5432111";
    private String ownerAccountPublicKey = "EOS6b5XruhrSdUE8k2dhKs88qhMUncNJYWCvuAuxkAgffWeavw1f3";
    private String walletName = "default";
    private String walletPassword = "PW5JV5VKP5Qhr4tCHZNjSsACeqXrSUQdSuqFWcvXCZoKuyLH9aty7";
    private static final String TOKEN = "eosio.token";
    private static final String SYL = "EOS";


    public Rpctest(){
        thisClient = EosApiClientFactory.newInstance(walletUrl, chainUrl, historyUrl).newRestClient();
        thisClient.openWallet(walletName);
        thisClient.unlockWallet(walletName, walletPassword);
    }
    private PushedTransaction signTransaction(String account, String name, String data){
        /* Create Transaction Action Authorization */
        TransactionAuthorization transactionAuthorization = new TransactionAuthorization();
        transactionAuthorization.setActor(ownerAccount);
        transactionAuthorization.setPermission("active");
        /* Get the head block */
        Block block = thisClient.getBlock(thisClient.getChainInfo().getHeadBlockId());
        /* Create Transaction Action */
        TransactionAction transactionAction = new TransactionAction();
        transactionAction.setAccount(account);
        transactionAction.setName(name);
//        transactionAction.setData(binargs);
        transactionAction.setAuthorization(Collections.singletonList(transactionAuthorization));

        /* Create a transaction */
        PackedTransaction packedTransaction = new PackedTransaction();
        packedTransaction.setRefBlockPrefix(block.getRefBlockPrefix().toString());
        packedTransaction.setRefBlockNum(block.getBlockNum().toString());
        packedTransaction.setExpiration("2018-12-10T18:38:19");
        packedTransaction.setRegion("0");
        packedTransaction.setMax_net_usage_words("0");
        packedTransaction.setActions(Collections.singletonList(transactionAction));

        /* Sign the Transaction */
        SignedPackedTransaction signedPackedTransaction = thisClient.signTransaction(packedTransaction, Collections.singletonList(ownerAccountPublicKey), thisClient.getChainInfo().getChainId());

        /* Push the transaction */
        PushedTransaction pushedTransaction = thisClient.pushTransaction("none", signedPackedTransaction);
        return pushedTransaction;
    }
    // 钱包功能之一，查询余额
    public List<String> getBanlance(String accountName) {
        return thisClient.getCurrencyBalance(TOKEN, accountName, SYL);
    }

    // 钱包功能之二，创建账户
    private String newAccountArgs(String name, String newOwnerKey, String newActiveKey){
        /* Create the json array of arguments */
        Map<String, String> args = new HashMap<>(4);
        args.put("creator", ownerAccount);
        args.put("name", name);
        args.put("OwnerKey", newOwnerKey);
        args.put("ActiveKey", newActiveKey);
        AbiJsonToBin data = thisClient.abiJsonToBin("eosio", "newaccount", args);
        return data.getBinargs();
    }
    public void newAccount(String name, String ownerKey, String activeKey){
        String binargs = newAccountArgs(name, ownerKey, activeKey);
        PushedTransaction pushedTransaction = signTransaction("eosio", "newaccount", binargs);
    }

    // 钱包功能之三，转账
    private String transferArgs(String from, String to, double amount, String memo){
        /* Create the json array of arguments */
        Map<String, String> args = new HashMap<>(4);
        args.put("from", from);
        args.put("to", to);
        args.put("quantity", new BigDecimalStringConverter().toString(new BigDecimal(amount).setScale(4, BigDecimal.ROUND_HALF_UP)) + " EOS");
        args.put("memo", memo);
        System.out.println(args.get("quantity"));
        AbiJsonToBin data = thisClient.abiJsonToBin(TOKEN, "transfer", args);
        return data.getBinargs();
    }

    public void transferTo(String to, double amount, String memo){

        /* Create the json array of arguments */
        String binargs = transferArgs(ownerAccount, to, amount, memo);

        PushedTransaction pushedTransaction = signTransaction(TOKEN, "transfer", binargs);
    }

    public Account getAccount(){return thisClient.getAccount("jrlee5432111");}

    public Account getAccount(String accountName) {return thisClient.getAccount(accountName); }

    public List<String> listWallets() {return thisClient.listWallets();}

    public static void main(String[] args){
        EosApiRestClient eosApiRestClient = new Rpctest().thisClient;
        System.out.println(eosApiRestClient.getChainInfo().getHeadBlockNum());

//        Test for wallets
//        eosApiRestClient.unlockWallet("default", "PW5JV5VKP5Qhr4tCHZNjSsACeqXrSUQdSuqFWcvXCZoKuyLH9aty7");
//        System.out.println(eosApiRestClient.listWallets());

        //  Test for create key
//        System.out.println(eosApiRestClient.createKey("default", WalletKeyType.R1));

        // Test for balances
        System.out.println(new Rpctest().getBanlance("jrlee5432111"));

        // Test for new Account
        new Rpctest().newAccount("newaccount11", "EOS8k35AxvRE1zycr42WiT1A2iVtM9oY5v7V4Do5EWTMu9nTeCapn",
                "EOS7U18FpPxiskEWBgYYmvEbXVdd87GzF9SRWpJ1Vq7GhnQakEi16");

        // Test for transfer
//        new Rpctest().transferTo("newaccount11", 20, "test1");

    }


}
