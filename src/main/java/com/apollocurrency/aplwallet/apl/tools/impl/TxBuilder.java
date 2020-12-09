package com.apollocurrency.aplwallet.apl.tools.impl;

import com.apollocurrency.aplwallet.api.dto.TransactionDTO;
import com.apollocurrency.aplwallet.apl.core.app.AplException;
import com.apollocurrency.aplwallet.apl.core.entity.blockchain.Transaction;
import com.apollocurrency.aplwallet.apl.core.entity.blockchain.TransactionImpl;
import com.apollocurrency.aplwallet.apl.core.entity.state.account.Account;
import com.apollocurrency.aplwallet.apl.core.entity.state.account.LedgerEvent;
import com.apollocurrency.aplwallet.apl.core.rest.converter.TransactionDTOConverter;
import com.apollocurrency.aplwallet.apl.core.rest.service.DexOrderAttachmentFactory;
import com.apollocurrency.aplwallet.apl.core.signature.DocumentSigner;
import com.apollocurrency.aplwallet.apl.core.signature.Signature;
import com.apollocurrency.aplwallet.apl.core.signature.SignatureToolFactory;
import com.apollocurrency.aplwallet.apl.core.transaction.CachedTransactionTypeFactory;
import com.apollocurrency.aplwallet.apl.core.transaction.TransactionBuilder;
import com.apollocurrency.aplwallet.apl.core.transaction.TransactionType;
import com.apollocurrency.aplwallet.apl.core.transaction.TransactionTypes;
import com.apollocurrency.aplwallet.apl.core.transaction.UnsupportedTransactionVersion;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.AbstractAttachment;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.AccountControlEffectiveBalanceLeasing;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.Attachment;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.ChildAccountAttachment;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.ColoredCoinsAskOrderCancellation;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.ColoredCoinsAskOrderPlacement;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.ColoredCoinsAssetDelete;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.ColoredCoinsAssetIssuance;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.ColoredCoinsAssetTransfer;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.ColoredCoinsBidOrderCancellation;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.ColoredCoinsBidOrderPlacement;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.ColoredCoinsDividendPayment;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.DexCloseOrderAttachment;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.DexContractAttachment;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.DexControlOfFrozenMoneyAttachment;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.DexOrderCancelAttachment;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.DigitalGoodsDelisting;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.DigitalGoodsDelivery;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.DigitalGoodsFeedback;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.DigitalGoodsListing;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.DigitalGoodsPriceChange;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.DigitalGoodsPurchase;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.DigitalGoodsQuantityChange;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.DigitalGoodsRefund;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.MessagingAccountInfo;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.MessagingAccountProperty;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.MessagingAccountPropertyDelete;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.MessagingAliasAssignment;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.MessagingAliasBuy;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.MessagingAliasDelete;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.MessagingAliasSell;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.MessagingPhasingVoteCasting;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.MessagingPollCreation;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.MessagingVoteCasting;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.MonetarySystemCurrencyDeletion;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.MonetarySystemCurrencyIssuance;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.MonetarySystemCurrencyMinting;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.MonetarySystemCurrencyTransfer;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.MonetarySystemExchangeBuyAttachment;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.MonetarySystemExchangeSell;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.MonetarySystemPublishExchangeOffer;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.MonetarySystemReserveClaim;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.MonetarySystemReserveIncrease;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.OrdinaryPaymentAttachment;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.PrivatePaymentAttachment;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.SetPhasingOnly;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.ShufflingCancellationAttachment;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.ShufflingCreation;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.ShufflingProcessingAttachment;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.ShufflingRecipientsAttachment;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.ShufflingRegistration;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.ShufflingVerificationAttachment;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.TaggedDataExtendAttachment;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.TaggedDataUploadAttachment;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.update.CriticalUpdate;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.update.ImportantUpdate;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.update.MinorUpdate;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.update.UpdateV2Attachment;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
public class TxBuilder {
    private final CachedTransactionTypeFactory factory;
    private final TransactionBuilder txBuilder;
    private final TransactionDTOConverter dtoConverter;
    private final DocumentSigner signer = SignatureToolFactory.selectBuilder(1).orElseThrow(UnsupportedTransactionVersion::new);
    private final long genesisCreatorId; // required for txs without recipient
    public TxBuilder(long genesisCreatorId) {

        this.genesisCreatorId = genesisCreatorId;
        this.factory = new CachedTransactionTypeFactory(Arrays.stream(TransactionTypes.TransactionTypeSpec.values()).map(BasicTransactionType::new).collect(Collectors.toList()));
        this.txBuilder = new TransactionBuilder(factory);
        this.dtoConverter = new TransactionDTOConverter(factory);
    }

    public Transaction buildAndSign(byte[] bytes, byte[] keySeed) throws AplException.NotValidException {
        TransactionImpl.BuilderImpl builder = txBuilder.newTransactionBuilder(bytes);

        return sign(builder.build(), keySeed);
    }

    public void setGenesisId(Transaction.Builder builder) throws AplException.NotValidException {
        if (builder.build().getRecipientId() == 0) {
            builder.recipientId(genesisCreatorId);
        }
    }

    public Transaction.Builder newTransactionBuilder(byte[] senderPublicKey, long amountATM, long feeATM, short deadline, Attachment attachment, int timestamp) throws AplException.NotValidException {
        Transaction.Builder builder = txBuilder.newTransactionBuilder(senderPublicKey, amountATM, feeATM, deadline, attachment, timestamp);
        return builder;
    }

    public Transaction sign(Transaction tx, byte[] keySeed) {
        Signature signature = signer.sign(tx.getUnsignedBytes(), SignatureToolFactory.createCredential(1, keySeed));
        tx.sign(signature);
        return tx;
    }

    public Transaction buildAndSign(TransactionDTO dto, byte[] keySeed) {
        return sign(dtoConverter.apply(dto), keySeed);
    }


    private static class BasicTransactionType extends TransactionType{
        private final TransactionTypes.TransactionTypeSpec spec;

        public BasicTransactionType(TransactionTypes.TransactionTypeSpec spec) {
            super(null, null);
            this.spec = spec;
        }

        @Override
        public TransactionTypes.TransactionTypeSpec getSpec() {
            return spec;
        }

        @Override
        public LedgerEvent getLedgerEvent() {
            return null;
        }

        @Override
        public AbstractAttachment parseAttachment(ByteBuffer buffer) throws AplException.NotValidException {
            switch (spec) {
                case ORDINARY_PAYMENT:
                    return new OrdinaryPaymentAttachment();
                case PRIVATE_PAYMENT:
                    return new PrivatePaymentAttachment();

                case ARBITRARY_MESSAGE:
                    return Attachment.ARBITRARY_MESSAGE;
                case ALIAS_ASSIGNMENT:
                    return new MessagingAliasAssignment(buffer);
                case POLL_CREATION:
                    return new MessagingPollCreation(buffer);
                case VOTE_CASTING:
                    return new MessagingVoteCasting(buffer);
                case HUB_ANNOUNCEMENT:
                    throw new RuntimeException("Unsupported tx type: Hub_Announcement");
                case ACCOUNT_INFO:
                    return new MessagingAccountInfo(buffer);
                case ALIAS_SELL:
                    return new MessagingAliasSell(buffer);
                case ALIAS_BUY:
                    return new MessagingAliasBuy(buffer);
                case ALIAS_DELETE:
                    return new MessagingAliasDelete(buffer);
                case PHASING_VOTE_CASTING:
                    return new MessagingPhasingVoteCasting(buffer);
                case ACCOUNT_PROPERTY:
                    return new MessagingAccountProperty(buffer);
                case ACCOUNT_PROPERTY_DELETE:
                    return new MessagingAccountPropertyDelete(buffer);

                case EFFECTIVE_BALANCE_LEASING:
                    return new AccountControlEffectiveBalanceLeasing(buffer);
                case SET_PHASING_ONLY:
                    return new SetPhasingOnly(buffer);

                case TAGGED_DATA_UPLOAD:
                    return new TaggedDataUploadAttachment(buffer);
                case TAGGED_DATA_EXTEND:
                    return new TaggedDataExtendAttachment(buffer);
                case SHUFFLING_CREATION:
                    return new ShufflingCreation(buffer);
                case SHUFFLING_REGISTRATION:
                    return new ShufflingRegistration(buffer);
                case SHUFFLING_PROCESSING:
                    return new ShufflingProcessingAttachment(buffer);
                case SHUFFLING_RECIPIENTS:
                    return new ShufflingRecipientsAttachment(buffer);
                case SHUFFLING_VERIFICATION:
                    return new ShufflingVerificationAttachment(buffer);
                case SHUFFLING_CANCELLATION:
                    return new ShufflingCancellationAttachment(buffer);

                case MS_CURRENCY_ISSUANCE:
                    return new MonetarySystemCurrencyIssuance(buffer);
                case MS_RESERVE_INCREASE:
                    return new MonetarySystemReserveIncrease(buffer);
                case MS_RESERVE_CLAIM:
                    return new MonetarySystemReserveClaim(buffer);
                case MS_CURRENCY_TRANSFER:
                    return new MonetarySystemCurrencyTransfer(buffer);
                case MS_PUBLISH_EXCHANGE_OFFER:
                    return new MonetarySystemPublishExchangeOffer(buffer);
                case MS_EXCHANGE_BUY:
                    return new MonetarySystemExchangeBuyAttachment(buffer);
                case MS_EXCHANGE_SELL:
                    return new MonetarySystemExchangeSell(buffer);
                case MS_CURRENCY_MINTING:
                    return new MonetarySystemCurrencyMinting(buffer);
                case MS_CURRENCY_DELETION:
                    return new MonetarySystemCurrencyDeletion(buffer);
                case CC_ASSET_ISSUANCE:
                    return new ColoredCoinsAssetIssuance(buffer);
                case CC_ASSET_TRANSFER:
                    return new ColoredCoinsAssetTransfer(buffer);
                case CC_ASK_ORDER_PLACEMENT:
                    return new ColoredCoinsAskOrderPlacement(buffer);
                case CC_BID_ORDER_PLACEMENT:
                    return new ColoredCoinsBidOrderPlacement(buffer);
                case CC_ASK_ORDER_CANCELLATION:
                    return new ColoredCoinsAskOrderCancellation(buffer);
                case CC_BID_ORDER_CANCELLATION:
                    return new ColoredCoinsBidOrderCancellation(buffer);
                case CC_DIVIDEND_PAYMENT:
                    return new ColoredCoinsDividendPayment(buffer);
                case CC_ASSET_DELETE:
                    return new ColoredCoinsAssetDelete(buffer);

                case DGS_LISTING:
                    return new DigitalGoodsListing(buffer);
                case DGS_DELISTING:
                    return new DigitalGoodsDelisting(buffer);
                case DGS_CHANGE_PRICE:
                    return new DigitalGoodsPriceChange(buffer);
                case DGS_CHANGE_QUANTITY:
                    return new DigitalGoodsQuantityChange(buffer);
                case DGS_PURCHASE:
                    return new DigitalGoodsPurchase(buffer);
                case DGS_DELIVERY:
                    return new DigitalGoodsDelivery(buffer);
                case DGS_FEEDBACK:
                    return new DigitalGoodsFeedback(buffer);
                case DGS_REFUND:
                    return new DigitalGoodsRefund(buffer);

                case CRITICAL_UPDATE:
                    return new CriticalUpdate(buffer);
                case IMPORTANT_UPDATE:
                    return new ImportantUpdate(buffer);
                case MINOR_UPDATE:
                    return new MinorUpdate(buffer);
                case UPDATE_V2:
                    return new UpdateV2Attachment(buffer);

                case DEX_ORDER:
                    return DexOrderAttachmentFactory.build(buffer);
                case DEX_CANCEL_ORDER:
                    return new DexOrderCancelAttachment(buffer);
                case DEX_CONTRACT:
                    return new DexContractAttachment(buffer);
                case DEX_TRANSFER_MONEY:
                    return new DexControlOfFrozenMoneyAttachment(buffer);
                case DEX_CLOSE_ORDER:
                    return new DexCloseOrderAttachment(buffer);

                case CHILD_ACCOUNT_CREATE:
                    return new ChildAccountAttachment(buffer);
                case CHILD_ACCOUNT_CONVERT_TO:
                    return new ChildAccountAttachment(buffer);
                default:
                    throw new RuntimeException("Unknown tx type: "  + spec);
            }
        }

        @Override
        public AbstractAttachment parseAttachment(JSONObject jsonObject) {
            switch (spec) {
                case ORDINARY_PAYMENT:
                    return new OrdinaryPaymentAttachment();
                case PRIVATE_PAYMENT:
                    return new PrivatePaymentAttachment();

                case ARBITRARY_MESSAGE:
                    return Attachment.ARBITRARY_MESSAGE;
                case ALIAS_ASSIGNMENT:
                    return new MessagingAliasAssignment(jsonObject);
                case POLL_CREATION:
                    return new MessagingPollCreation(jsonObject);
                case VOTE_CASTING:
                    return new MessagingVoteCasting(jsonObject);
                case HUB_ANNOUNCEMENT:
                    throw new RuntimeException("Unsupported tx type: Hub_Announcement");
                case ACCOUNT_INFO:
                    return new MessagingAccountInfo(jsonObject);
                case ALIAS_SELL:
                    return new MessagingAliasSell(jsonObject);
                case ALIAS_BUY:
                    return new MessagingAliasBuy(jsonObject);
                case ALIAS_DELETE:
                    return new MessagingAliasDelete(jsonObject);
                case PHASING_VOTE_CASTING:
                    return new MessagingPhasingVoteCasting(jsonObject);
                case ACCOUNT_PROPERTY:
                    return new MessagingAccountProperty(jsonObject);
                case ACCOUNT_PROPERTY_DELETE:
                    return new MessagingAccountPropertyDelete(jsonObject);

                case EFFECTIVE_BALANCE_LEASING:
                    return new AccountControlEffectiveBalanceLeasing(jsonObject);
                case SET_PHASING_ONLY:
                    return new SetPhasingOnly(jsonObject);

                case TAGGED_DATA_UPLOAD:
                    return new TaggedDataUploadAttachment(jsonObject);
                case TAGGED_DATA_EXTEND:
                    return new TaggedDataExtendAttachment(jsonObject);
                case SHUFFLING_CREATION:
                    return new ShufflingCreation(jsonObject);
                case SHUFFLING_REGISTRATION:
                    return new ShufflingRegistration(jsonObject);
                case SHUFFLING_PROCESSING:
                    return new ShufflingProcessingAttachment(jsonObject);
                case SHUFFLING_RECIPIENTS:
                    return new ShufflingRecipientsAttachment(jsonObject);
                case SHUFFLING_VERIFICATION:
                    return new ShufflingVerificationAttachment(jsonObject);
                case SHUFFLING_CANCELLATION:
                    return new ShufflingCancellationAttachment(jsonObject);

                case MS_CURRENCY_ISSUANCE:
                    return new MonetarySystemCurrencyIssuance(jsonObject);
                case MS_RESERVE_INCREASE:
                    return new MonetarySystemReserveIncrease(jsonObject);
                case MS_RESERVE_CLAIM:
                    return new MonetarySystemReserveClaim(jsonObject);
                case MS_CURRENCY_TRANSFER:
                    return new MonetarySystemCurrencyTransfer(jsonObject);
                case MS_PUBLISH_EXCHANGE_OFFER:
                    return new MonetarySystemPublishExchangeOffer(jsonObject);
                case MS_EXCHANGE_BUY:
                    return new MonetarySystemExchangeBuyAttachment(jsonObject);
                case MS_EXCHANGE_SELL:
                    return new MonetarySystemExchangeSell(jsonObject);
                case MS_CURRENCY_MINTING:
                    return new MonetarySystemCurrencyMinting(jsonObject);
                case MS_CURRENCY_DELETION:
                    return new MonetarySystemCurrencyDeletion(jsonObject);
                case CC_ASSET_ISSUANCE:
                    return new ColoredCoinsAssetIssuance(jsonObject);
                case CC_ASSET_TRANSFER:
                    return new ColoredCoinsAssetTransfer(jsonObject);
                case CC_ASK_ORDER_PLACEMENT:
                    return new ColoredCoinsAskOrderPlacement(jsonObject);
                case CC_BID_ORDER_PLACEMENT:
                    return new ColoredCoinsBidOrderPlacement(jsonObject);
                case CC_ASK_ORDER_CANCELLATION:
                    return new ColoredCoinsAskOrderCancellation(jsonObject);
                case CC_BID_ORDER_CANCELLATION:
                    return new ColoredCoinsBidOrderCancellation(jsonObject);
                case CC_DIVIDEND_PAYMENT:
                    return new ColoredCoinsDividendPayment(jsonObject);
                case CC_ASSET_DELETE:
                    return new ColoredCoinsAssetDelete(jsonObject);

                case DGS_LISTING:
                    return new DigitalGoodsListing(jsonObject);
                case DGS_DELISTING:
                    return new DigitalGoodsDelisting(jsonObject);
                case DGS_CHANGE_PRICE:
                    return new DigitalGoodsPriceChange(jsonObject);
                case DGS_CHANGE_QUANTITY:
                    return new DigitalGoodsQuantityChange(jsonObject);
                case DGS_PURCHASE:
                    return new DigitalGoodsPurchase(jsonObject);
                case DGS_DELIVERY:
                    return new DigitalGoodsDelivery(jsonObject);
                case DGS_FEEDBACK:
                    return new DigitalGoodsFeedback(jsonObject);
                case DGS_REFUND:
                    return new DigitalGoodsRefund(jsonObject);

                case CRITICAL_UPDATE:
                    return new CriticalUpdate(jsonObject);
                case IMPORTANT_UPDATE:
                    return new ImportantUpdate(jsonObject);
                case MINOR_UPDATE:
                    return new MinorUpdate(jsonObject);
                case UPDATE_V2:
                    return new UpdateV2Attachment(jsonObject);

                case DEX_ORDER:
                    return DexOrderAttachmentFactory.parse(jsonObject);
                case DEX_CANCEL_ORDER:
                    return new DexOrderCancelAttachment(jsonObject);
                case DEX_CONTRACT:
                    return new DexContractAttachment(jsonObject);
                case DEX_TRANSFER_MONEY:
                    return new DexControlOfFrozenMoneyAttachment(jsonObject);
                case DEX_CLOSE_ORDER:
                    return new DexCloseOrderAttachment(jsonObject);

                case CHILD_ACCOUNT_CREATE:
                    return new ChildAccountAttachment(jsonObject);
                case CHILD_ACCOUNT_CONVERT_TO:
                    return new ChildAccountAttachment(jsonObject);
                default:
                    throw new RuntimeException("Unknown tx type: "  + spec);
            }
        }

        @Override
        public void doStateDependentValidation(Transaction transaction) throws AplException.ValidationException {

        }

        @Override
        public void doStateIndependentValidation(Transaction transaction) throws AplException.ValidationException {

        }

        @Override
        public boolean applyAttachmentUnconfirmed(Transaction transaction, Account account) {
            return true;
        }

        @Override
        public void applyAttachment(Transaction transaction, Account account, Account account1) {

        }

        @Override
        public void undoAttachmentUnconfirmed(Transaction transaction, Account account) {

        }

        @Override
        public boolean canHaveRecipient() {
            return true;
        }

        @Override
        public boolean isPhasingSafe() {
            return true;
        }

        @Override
        public String getName() {
            return "BasicTransactionType";
        }
    }

}
