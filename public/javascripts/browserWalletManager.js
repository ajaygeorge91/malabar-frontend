
import * as wasm from "https://cdn.jsdelivr.net/npm/@emurgo/cardano-serialization-lib-asmjs@9.1.2/cardano_serialization_lib.min.js";

$(function () {

    function hexToBytes(hex) {
        for (var bytes = [], c = 0; c < hex.length; c += 2)
            bytes.push(parseInt(hex.substr(c, 2), 16));
        return bytes;
    }

    function getAdaBalance(balanceHex) {
        const balanceValue = wasm.Value.from_bytes(hexToBytes(balanceHex));
        const lovelaces = balanceValue.coin().to_str();
        const balance = parseFloat(lovelaces)/1000000;
        return balance;
    }

    async function setWallet(walletNs) {
        await localStorage.setItem("selected_wallet_ns", walletNs);
    }
    function getWallet() {
        return localStorage.getItem("selected_wallet_ns");
    }

    var enabledWalletTemplate = document.getElementById('enabled_wallet');
    var availableWalletTemplate = document.getElementById('available_wallet');
    var selectedWalletTemplate = document.getElementById('selected_wallet');
    var loadingWalletTemplate = document.getElementById('loading_wallet');

    async function getCardanoData() {
        if(window.cardano.ccvault){
            const isEnabled = await window.cardano.ccvault.isEnabled();
            const data = {
                               ns : "ccvault",
                               name : window.cardano.ccvault.name,
                               image : window.cardano.ccvault.icon,
                               balance : "",
                           }
            if(isEnabled){
                $('#ccvault_space').html(Mustache.render(loadingWalletTemplate.innerHTML, data));
                const wallet = await window.cardano.ccvault.enable();
                const balanceHex = await wallet.getBalance();
                const balance = getAdaBalance(balanceHex);
                let newData = data;
                newData.balance = balance
                if(getWallet() == "ccvault"){
                    $('#ccvault_space').html(Mustache.render(selectedWalletTemplate.innerHTML, newData));
                }else{
                    $('#ccvault_space').html(Mustache.render(enabledWalletTemplate.innerHTML, newData));
                }
            }else{
                $('#ccvault_space').html(Mustache.render(availableWalletTemplate.innerHTML, data));
            }
        }
    }
    async function getNamiData() {
        if(window.cardano.nami){
            const isEnabled = await window.cardano.nami.isEnabled();
            const data = {
                             ns : "nami",
                             name : window.cardano.nami.name,
                             image : window.cardano.nami.icon,
                             balance : "",
                         }
            $('#nami_space').html(Mustache.render(loadingWalletTemplate.innerHTML, data));
            if(isEnabled){
                const wallet = await window.cardano.nami.enable();
                const balanceHex = await wallet.getBalance();
                const balance = getAdaBalance(balanceHex);
                let newData = data;
                newData.balance = balance
                if(getWallet() == "nami"){
                    $('#nami_space').html(Mustache.render(selectedWalletTemplate.innerHTML, newData));
                }else{
                    $('#nami_space').html(Mustache.render(enabledWalletTemplate.innerHTML, newData));
                }
            }else{
                 $('#nami_space').html(Mustache.render(availableWalletTemplate.innerHTML, data));
             }
        }
    }
    async function getYoroiData() {
        if(window.cardano.yoroi){
            const isEnabled = await window.cardano.yoroi.isEnabled();
            let balance = "";
            const data = {
                             ns : "yoroi",
                             name : window.cardano.yoroi.name,
                             image : window.cardano.yoroi.icon,
                             balance : "",
                         }
            $('#yoroi_space').html(Mustache.render(loadingWalletTemplate.innerHTML, data));
            if(isEnabled){
                const wallet = await window.cardano.yoroi.enable();
                const balanceHex = await wallet.getBalance();
                const balance = getAdaBalance(balanceHex);
                let newData = data;
                newData.balance = balance
                if(getWallet() == "yoroi"){
                    $('#yoroi_space').html(Mustache.render(selectedWalletTemplate.innerHTML, newData));
                }else{
                    $('#yoroi_space').html(Mustache.render(enabledWalletTemplate.innerHTML, newData));
                }
            }else{
                $('#yoroi_space').html(Mustache.render(availableWalletTemplate.innerHTML, data));
            }
        }
    }
    async function getMalabarData() {
        if(window.cardano.malabar){
            const isEnabled = await window.cardano.malabar.isEnabled();
            let balance = "";
            const data = {
                             ns : "malabar",
                             name : window.cardano.malabar.name,
                             image : window.cardano.malabar.icon,
                             balance : "",
                         }
            $('#malabar_space').html(Mustache.render(loadingWalletTemplate.innerHTML, data));
            if(isEnabled){
                const wallet = await window.cardano.malabar.enable();
                const balanceHex = await wallet.getBalance();
                const balance = getAdaBalance(balanceHex);
                let newData = data;
                newData.balance = balance
                if(getWallet() == "malabar"){
                    $('#malabar_space').html(Mustache.render(selectedWalletTemplate.innerHTML, newData));
                }else{
                    $('#malabar_space').html(Mustache.render(enabledWalletTemplate.innerHTML, newData));
                }
            }else{
                $('#malabar_space').html(Mustache.render(availableWalletTemplate.innerHTML, data));
            }
        }
    }

    async function getAllWalletData() {
        await Promise.all([getNamiData(), getYoroiData(), getCardanoData(), getMalabarData()]);
    }
    getAllWalletData();



    $("#ccvault_space").on('click', '.enable_button' ,function(){
        window.cardano.ccvault.enable().then(f => {
            getAllWalletData();
        })
    });
    $("#nami_space").on('click', '.enable_button' ,function(){
        window.cardano.nami.enable().then(f => {
            getAllWalletData();
        })
    });
    $("#yoroi_space").on('click', '.enable_button' ,function(){
        window.cardano.yoroi.enable().then(f => {
            getAllWalletData();
        })
    });

    $("#ccvault_space").on('click', '.select_button' ,function(){
        setWallet("ccvault");
        Promise.all([getAllWalletData(), setWalletAddress()]);
    });
    $("#nami_space").on('click', '.select_button' ,function(){
        setWallet("nami");
        Promise.all([getAllWalletData(), setWalletAddress()]);
    });
    $("#yoroi_space").on('click', '.select_button' ,function(){
        setWallet("yoroi");
        Promise.all([getAllWalletData(), setWalletAddress()]);
    });


    async function getSelectedWallet() {
        let wallet = null;
        if(getWallet() == "nami"){
            if(await window.cardano.nami.isEnabled()){
                wallet = await window.cardano.nami.enable();
            }
        } else if(getWallet() == "ccvault"){
            if(await window.cardano.ccvault.isEnabled()){
                wallet = await window.cardano.ccvault.enable();
            }
        } else if(getWallet() == "yoroi"){
            if(await window.cardano.yoroi.isEnabled()){
                wallet = await window.cardano.yoroi.enable();
            }
        }
        console.log(wallet);
        return wallet;
    }

    async function setWalletAddress() {
        const wallet = await getSelectedWallet();
        console.log(wallet);
        const addresses = await wallet.getUsedAddresses();
        const addressList = addresses.map(r => {
            const address = wasm.Address.from_bytes(hexToBytes(r)).to_bech32();
            return address;
        });
        const addressListHtml = addressList.map(address => {
            return "<li>"+ address +"</li>";
        });
        $('#demo3_addressList').html(addressListHtml);
        $('#card-addresses').val(addressList.join(","));
    }

    setWalletAddress();


    async function signTransactionIfAvailable() {
        const transactionToSign = $("#card-transactionToSign").val();
            console.log(transactionToSign);
            if(transactionToSign){
                const wallet = await getSelectedWallet();
                console.log(wallet);
                if(wallet){
                    const witnessSet = await wallet.signTx(transactionToSign, true);
                    console.log(witnessSet);
                    $('#card-witnessSet').val(witnessSet);
                }else {
                    alert("no wallet!");
                }

            }
    }
    signTransactionIfAvailable();




//        window.cardano.ccvault.enable().then((a) => {
//          console.log(a);
//          a.getBalance().then((res) => {
//            const balance = wasm.Value.from_bytes(hexToBytes(res));
//            const lovelaces = balance.coin().to_str();
//            $('#demo2').html(lovelaces);
//          });
//          a.getChangeAddress().then((res) => {
//            const address = wasm.Address.from_bytes(hexToBytes(res)).to_bech32();
//            console.log(address);
//            $('#demo3').html(address);
//          });
//          a.getUsedAddresses().then((res) => {
//            const addressList = res.map(r => {
//                const address = wasm.Address.from_bytes(hexToBytes(r)).to_bech32();
//                return "<li>"+ address +"</li>";
//            });
//            $('#demo3_addressList').html(addressList);
//          });
//          const txnSigned = "a10081825820b82de6c4cea6db79defdc850681382c2072fcd4f443b2d34553ea8c1bf7531d058405b5deaa554e42543d444c8b91fee478fab3c792d090676dae9ffeae4a902ac49879a4689cd6d567f13e8ddb4e421699d9fa6cfcef060a63988e80fd6fa11fa09"
//          const transactionWitnessSet = wasm.TransactionWitnessSet.from_bytes(hexToBytes(txnSigned));
//          console.log(transactionWitnessSet);
//        });



});
