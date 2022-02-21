/*
 * Copyright © 2018-2022 Apollo Foundation
 */

package com.apollocurrency.aplwallet.apl.tools.impl;

import com.apollocurrency.aplwallet.api.dto.TransactionDTO;
import com.apollocurrency.aplwallet.apl.core.chainid.BlockchainConfig;
import com.apollocurrency.aplwallet.apl.core.entity.state.account.Account;
import com.apollocurrency.aplwallet.apl.core.entity.state.account.LedgerEvent;
import com.apollocurrency.aplwallet.apl.core.model.Transaction;
import com.apollocurrency.aplwallet.apl.core.model.TransactionImpl;
import com.apollocurrency.aplwallet.apl.core.rest.service.DexOrderAttachmentFactory;
import com.apollocurrency.aplwallet.apl.core.rest.service.PhasingAppendixFactory;
import com.apollocurrency.aplwallet.apl.core.service.blockchain.TransactionBuilderFactory;
import com.apollocurrency.aplwallet.apl.core.signature.DocumentSigner;
import com.apollocurrency.aplwallet.apl.core.signature.Signature;
import com.apollocurrency.aplwallet.apl.core.signature.SignatureParser;
import com.apollocurrency.aplwallet.apl.core.signature.SignatureToolFactory;
import com.apollocurrency.aplwallet.apl.core.transaction.CachedTransactionTypeFactory;
import com.apollocurrency.aplwallet.apl.core.transaction.TransactionType;
import com.apollocurrency.aplwallet.apl.core.transaction.TransactionTypes;
import com.apollocurrency.aplwallet.apl.core.transaction.TransactionWrapperHelper;
import com.apollocurrency.aplwallet.apl.core.transaction.UnsupportedTransactionVersion;
import com.apollocurrency.aplwallet.apl.core.transaction.common.TxBContext;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.AbstractAttachment;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.AccountControlEffectiveBalanceLeasing;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.Attachment;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.CCAskOrderPlacementAttachment;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.CCAssetDeleteAttachment;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.CCAssetTransferAttachment;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.CCBidOrderPlacementAttachment;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.CCDividendPaymentAttachment;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.ChildAccountAttachment;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.ColoredCoinsAskOrderCancellation;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.ColoredCoinsAssetIssuance;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.ColoredCoinsBidOrderCancellation;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.DGSDelistingAttachment;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.DGSDeliveryAttachment;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.DGSFeedbackAttachment;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.DGSListingAttachment;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.DGSPriceChangeAttachment;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.DGSPurchaseAttachment;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.DGSQuantityChangeAttachment;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.DGSRefundAttachment;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.DexCloseOrderAttachment;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.DexContractAttachment;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.DexControlOfFrozenMoneyAttachment;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.DexOrderCancelAttachment;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.EncryptToSelfMessageAppendix;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.EncryptedMessageAppendix;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.MSCurrencyTransferAttachment;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.MSExchangeSellAttachment;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.MSPublishExchangeOfferAttachment;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.MSReserveClaimAttachment;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.MessageAppendix;
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
import com.apollocurrency.aplwallet.apl.core.transaction.messages.MonetarySystemCurrencyIssuanceAttachment;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.MonetarySystemCurrencyMinting;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.MonetarySystemExchangeBuyAttachment;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.MonetarySystemReserveIncreaseAttachment;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.OrdinaryPaymentAttachment;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.PrivatePaymentAttachment;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.PrunableEncryptedMessageAppendix;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.PrunablePlainMessageAppendix;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.PublicKeyAnnouncementAppendix;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.SetPhasingOnly;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.ShufflingCancellationAttachment;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.ShufflingCreationAttachment;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.ShufflingProcessingAttachment;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.ShufflingRecipientsAttachment;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.ShufflingRegistrationAttachment;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.ShufflingVerificationAttachment;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.TaggedDataExtendAttachment;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.TaggedDataUploadAttachment;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.update.CriticalUpdate;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.update.ImportantUpdate;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.update.MinorUpdate;
import com.apollocurrency.aplwallet.apl.core.transaction.messages.update.UpdateV2Attachment;
import com.apollocurrency.aplwallet.apl.core.utils.CollectionUtil;
import com.apollocurrency.aplwallet.apl.crypto.Convert;
import com.apollocurrency.aplwallet.apl.util.StringUtils;
import com.apollocurrency.aplwallet.apl.util.env.config.BlockchainProperties;
import com.apollocurrency.aplwallet.apl.util.env.config.Chain;
import com.apollocurrency.aplwallet.apl.util.env.config.FeaturesHeightRequirement;
import com.apollocurrency.aplwallet.apl.util.exception.AplException;
import com.apollocurrency.aplwallet.apl.util.injectable.PropertiesHolder;
import com.apollocurrency.aplwallet.apl.util.io.PayloadResult;
import com.apollocurrency.aplwallet.apl.util.io.Result;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONObject;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
public class TxBuilder {
    private final CachedTransactionTypeFactory factory;
    private final TransactionBuilderFactory txBuilder;
    private final DocumentSigner signer = SignatureToolFactory.selectSigner(1).orElseThrow(UnsupportedTransactionVersion::new);
    private final long genesisCreatorId; // required for txs without recipient

    // Mock chain because of NPE in BlockchainConfig init when empty chain supplied
    private static final UUID MOCKED_CHAIN_ID = UUID.fromString("3fecf3bd-86a3-436b-a1d6-41eefc0bd1c6");
    private static final List<BlockchainProperties> MOCKED_BLOCKCHAIN_PROPERTIES1 = Collections.singletonList(
        new BlockchainProperties(0, 255, 160, 1, 60, 67,
            53, 30000000000L));
    private static final Chain MOCKED_CHAIN = new Chain(MOCKED_CHAIN_ID, true, Collections.emptyList(), Collections.emptyList(),
        Collections.emptyList(),
        "Apollo mock net",
        "Mock network to stub BlockchainConfig init", "Apollo",
        "APL", "Apollo",
        30000000000L, 8,
        MOCKED_BLOCKCHAIN_PROPERTIES1, new FeaturesHeightRequirement(), Set.of(), Set.of());



    public TxBuilder(long genesisCreatorId) {
        this.genesisCreatorId = genesisCreatorId;
        this.factory = new CachedTransactionTypeFactory(Arrays.stream(TransactionTypes.TransactionTypeSpec.values()).map(BasicTransactionType::new).collect(Collectors.toList()));
        this.txBuilder = new TransactionBuilderFactory(factory, new BlockchainConfig(MOCKED_CHAIN, new PropertiesHolder()));
    }

    public Transaction buildAndSign(byte[] bytes, byte[] keySeed) {
        Transaction tx = build(bytes);

        return sign(tx, keySeed);
    }

    public Transaction build(byte[] bytes) {
        Transaction transaction = createTxFrom(bytes);
        TransactionImpl.Builder builder = newTransactionBuilder(transaction.getSenderPublicKey(), transaction.getAmountATM(), transaction.getFeeATM(), transaction.getDeadline(), transaction.getAttachment(), transaction.getTimestamp());
        builder.ecBlockHeight(transaction.getECBlockHeight());
        builder.ecBlockId(transaction.getECBlockId());
        builder.signature(transaction.getSignature());
        builder.appendix(transaction.getMessage());
        builder.appendix(transaction.getPublicKeyAnnouncement());
        builder.appendix(transaction.getEncryptToSelfMessage());
        builder.appendix(transaction.getPrunableEncryptedMessage());
        builder.appendix(transaction.getPrunablePlainMessage());
        builder.appendix(transaction.getEncryptedMessage());
        builder.appendix(transaction.getPhasing());
        builder.recipientId(transaction.getRecipientId());
        setGenesisId(builder);
        return builder.build();
    }

    /**
     * It is a workaround to set recipient id of transaction without recipient to same value as {@link com.apollocurrency.aplwallet.apl.core.app.GenesisImporter#CREATOR_ID} offer,
     * by checking recipient field (should be zero)
     * @param builder transaction builder to set recipient to genesisCreatorId
     */
    public void setGenesisId(Transaction.Builder builder) {
        if (builder.build().getRecipientId() == 0) {
            builder.recipientId(genesisCreatorId);
        }
    }

    public Result toUnsignedBytes(Transaction tx) {
        Result result = PayloadResult.createLittleEndianByteArrayResult();

        TxBContext instance = TxBContext.newInstance(MOCKED_CHAIN);
        instance.createSerializer(tx.getVersion()).serialize(TransactionWrapperHelper.createUnsignedTransaction(tx), result);

        return result;
    }

    public Result toBytes(Transaction tx) {
        Result result = PayloadResult.createLittleEndianByteArrayResult();

        TxBContext instance = TxBContext.newInstance(MOCKED_CHAIN);
        instance.createSerializer(tx.getVersion()).serialize(tx, result);

        return result;
    }

    public Transaction.Builder newTransactionBuilder(byte[] senderPublicKey, long amountATM, long feeATM, short deadline, Attachment attachment, int timestamp) {
        Transaction.Builder builder = txBuilder.newUnsignedTransactionBuilder(1, senderPublicKey, amountATM, feeATM, deadline, attachment, timestamp);
        return builder;
    }

    public Transaction sign(Transaction tx, byte[] keySeed) {
        Result unsignedBytes = toUnsignedBytes(tx);
        Signature signature = signer.sign(unsignedBytes.array(), SignatureToolFactory.createCredential(1, keySeed));

        ((TransactionImpl) tx.getTransactionImpl()).sign(signature, unsignedBytes);
        return tx;
    }

    public Transaction dtoToTx(TransactionDTO dto) {
        if (StringUtils.isBlank(dto.getRecipient())) {
            dto.setRecipient(Long.toUnsignedString(genesisCreatorId));
        }
        return convertTransactionFromDTO(dto);
    }

    /**
     * Used to support conversion of unsigned transactions
     * @param txDto parsed from json tx dto
     * @return transaction object (signed/unsigned)
     */
    public Transaction convertTransactionFromDTO(TransactionDTO txDto) {
        try {
            byte[] senderPublicKey = Convert.parseHexString(txDto.getSenderPublicKey());
            byte version = txDto.getVersion() == null ? 0 : txDto.getVersion();
            Signature signature = null;
            if (StringUtils.isNotBlank(txDto.getSignature())) {
                SignatureParser signatureParser = SignatureToolFactory.selectParser(version).orElseThrow(UnsupportedTransactionVersion::new);
                signature = signatureParser.parse(Convert.parseHexString(txDto.getSignature()));
            }

            int ecBlockHeight = 0;
            long ecBlockId = 0;
            if (version > 0) {
                ecBlockHeight = txDto.getEcBlockHeight();
                ecBlockId = Convert.parseUnsignedLong(txDto.getEcBlockId());
            }

            TransactionType transactionType = factory.findTransactionType(txDto.getType(), txDto.getSubtype());
            if (transactionType == null) {
                throw new AplException.NotValidException("Invalid transaction type: " + txDto.getType() + ", " + txDto.getSubtype());
            }

            JSONObject attachmentData;
            if (!CollectionUtil.isEmpty(txDto.getAttachment())) {
                attachmentData = new JSONObject(txDto.getAttachment());
            } else {
                throw new AplException.NotValidException("Transaction dto {" + txDto + "} has no attachment");
            }

            AbstractAttachment attachment = transactionType.parseAttachment(attachmentData);
            attachment.bindTransactionType(transactionType);
            TransactionImpl.BuilderImpl builder = new TransactionImpl.BuilderImpl(version, senderPublicKey,
                Convert.parseLong(txDto.getAmountATM()),
                Convert.parseLong(txDto.getFeeATM()),
                txDto.getDeadline(),
                attachment, txDto.getTimestamp(), transactionType)
                .referencedTransactionFullHash(txDto.getReferencedTransactionFullHash())
                .signature(signature)
                .ecBlockHeight(ecBlockHeight)
                .ecBlockId(ecBlockId);
            if (transactionType.canHaveRecipient()) {
                long recipientId = Convert.parseUnsignedLong(txDto.getRecipient());
                builder.recipientId(recipientId);
            }

            builder.appendix(MessageAppendix.parse(attachmentData));
            builder.appendix(EncryptedMessageAppendix.parse(attachmentData));
            builder.appendix(PublicKeyAnnouncementAppendix.parse(attachmentData));
            builder.appendix(EncryptToSelfMessageAppendix.parse(attachmentData));
            builder.appendix(PhasingAppendixFactory.parse(attachmentData));
            builder.appendix(PrunablePlainMessageAppendix.parse(attachmentData));
            builder.appendix(PrunableEncryptedMessageAppendix.parse(attachmentData));

            return builder.build();
        } catch (RuntimeException | AplException.NotValidException e) {
            log.debug("Failed to parse transaction: " + txDto.toString());
            throw new RuntimeException(e);
        }
    }

    private Transaction createTxFrom(byte[] bytes) {
        Transaction tx;
        try {
            tx = txBuilder.newTransaction(bytes);
        } catch (AplException.NotValidException e) {
            throw new RuntimeException("Unable to create transaction from bytes: " + Convert.toHexString(bytes), e);
        }
        return tx;
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
                    return new ShufflingCreationAttachment(buffer);
                case SHUFFLING_REGISTRATION:
                    return new ShufflingRegistrationAttachment(buffer);
                case SHUFFLING_PROCESSING:
                    return new ShufflingProcessingAttachment(buffer);
                case SHUFFLING_RECIPIENTS:
                    return new ShufflingRecipientsAttachment(buffer);
                case SHUFFLING_VERIFICATION:
                    return new ShufflingVerificationAttachment(buffer);
                case SHUFFLING_CANCELLATION:
                    return new ShufflingCancellationAttachment(buffer);

                case MS_CURRENCY_ISSUANCE:
                    return new MonetarySystemCurrencyIssuanceAttachment(buffer);
                case MS_RESERVE_INCREASE:
                    return new MonetarySystemReserveIncreaseAttachment(buffer);
                case MS_RESERVE_CLAIM:
                    return new MSReserveClaimAttachment(buffer);
                case MS_CURRENCY_TRANSFER:
                    return new MSCurrencyTransferAttachment(buffer);
                case MS_PUBLISH_EXCHANGE_OFFER:
                    return new MSPublishExchangeOfferAttachment(buffer);
                case MS_EXCHANGE_BUY:
                    return new MonetarySystemExchangeBuyAttachment(buffer);
                case MS_EXCHANGE_SELL:
                    return new MSExchangeSellAttachment(buffer);
                case MS_CURRENCY_MINTING:
                    return new MonetarySystemCurrencyMinting(buffer);
                case MS_CURRENCY_DELETION:
                    return new MonetarySystemCurrencyDeletion(buffer);
                case CC_ASSET_ISSUANCE:
                    return new ColoredCoinsAssetIssuance(buffer);
                case CC_ASSET_TRANSFER:
                    return new CCAssetTransferAttachment(buffer);
                case CC_ASK_ORDER_PLACEMENT:
                    return new CCAskOrderPlacementAttachment(buffer);
                case CC_BID_ORDER_PLACEMENT:
                    return new CCBidOrderPlacementAttachment(buffer);
                case CC_ASK_ORDER_CANCELLATION:
                    return new ColoredCoinsAskOrderCancellation(buffer);
                case CC_BID_ORDER_CANCELLATION:
                    return new ColoredCoinsBidOrderCancellation(buffer);
                case CC_DIVIDEND_PAYMENT:
                    return new CCDividendPaymentAttachment(buffer);
                case CC_ASSET_DELETE:
                    return new CCAssetDeleteAttachment(buffer);

                case DGS_LISTING:
                    return new DGSListingAttachment(buffer);
                case DGS_DELISTING:
                    return new DGSDelistingAttachment(buffer);
                case DGS_CHANGE_PRICE:
                    return new DGSPriceChangeAttachment(buffer);
                case DGS_CHANGE_QUANTITY:
                    return new DGSQuantityChangeAttachment(buffer);
                case DGS_PURCHASE:
                    return new DGSPurchaseAttachment(buffer);
                case DGS_DELIVERY:
                    return new DGSDeliveryAttachment(buffer);
                case DGS_FEEDBACK:
                    return new DGSFeedbackAttachment(buffer);
                case DGS_REFUND:
                    return new DGSRefundAttachment(buffer);

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
                    return new ShufflingCreationAttachment(jsonObject);
                case SHUFFLING_REGISTRATION:
                    return new ShufflingRegistrationAttachment(jsonObject);
                case SHUFFLING_PROCESSING:
                    return new ShufflingProcessingAttachment(jsonObject);
                case SHUFFLING_RECIPIENTS:
                    return new ShufflingRecipientsAttachment(jsonObject);
                case SHUFFLING_VERIFICATION:
                    return new ShufflingVerificationAttachment(jsonObject);
                case SHUFFLING_CANCELLATION:
                    return new ShufflingCancellationAttachment(jsonObject);

                case MS_CURRENCY_ISSUANCE:
                    return new MonetarySystemCurrencyIssuanceAttachment(jsonObject);
                case MS_RESERVE_INCREASE:
                    return new MonetarySystemReserveIncreaseAttachment(jsonObject);
                case MS_RESERVE_CLAIM:
                    return new MSReserveClaimAttachment(jsonObject);
                case MS_CURRENCY_TRANSFER:
                    return new MSCurrencyTransferAttachment(jsonObject);
                case MS_PUBLISH_EXCHANGE_OFFER:
                    return new MSPublishExchangeOfferAttachment(jsonObject);
                case MS_EXCHANGE_BUY:
                    return new MonetarySystemExchangeBuyAttachment(jsonObject);
                case MS_EXCHANGE_SELL:
                    return new MSExchangeSellAttachment(jsonObject);
                case MS_CURRENCY_MINTING:
                    return new MonetarySystemCurrencyMinting(jsonObject);
                case MS_CURRENCY_DELETION:
                    return new MonetarySystemCurrencyDeletion(jsonObject);
                case CC_ASSET_ISSUANCE:
                    return new ColoredCoinsAssetIssuance(jsonObject);
                case CC_ASSET_TRANSFER:
                    return new CCAssetTransferAttachment(jsonObject);
                case CC_ASK_ORDER_PLACEMENT:
                    return new CCAskOrderPlacementAttachment(jsonObject);
                case CC_BID_ORDER_PLACEMENT:
                    return new CCBidOrderPlacementAttachment(jsonObject);
                case CC_ASK_ORDER_CANCELLATION:
                    return new ColoredCoinsAskOrderCancellation(jsonObject);
                case CC_BID_ORDER_CANCELLATION:
                    return new ColoredCoinsBidOrderCancellation(jsonObject);
                case CC_DIVIDEND_PAYMENT:
                    return new CCDividendPaymentAttachment(jsonObject);
                case CC_ASSET_DELETE:
                    return new CCAssetDeleteAttachment(jsonObject);

                case DGS_LISTING:
                    return new DGSListingAttachment(jsonObject);
                case DGS_DELISTING:
                    return new DGSDelistingAttachment(jsonObject);
                case DGS_CHANGE_PRICE:
                    return new DGSPriceChangeAttachment(jsonObject);
                case DGS_CHANGE_QUANTITY:
                    return new DGSQuantityChangeAttachment(jsonObject);
                case DGS_PURCHASE:
                    return new DGSPurchaseAttachment(jsonObject);
                case DGS_DELIVERY:
                    return new DGSDeliveryAttachment(jsonObject);
                case DGS_FEEDBACK:
                    return new DGSFeedbackAttachment(jsonObject);
                case DGS_REFUND:
                    return new DGSRefundAttachment(jsonObject);

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
        public void doStateDependentValidation(Transaction transaction) {

        }

        @Override
        public void doStateIndependentValidation(Transaction transaction) {

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

        /**
         * @return always true to make it possible to use {@link com.apollocurrency.aplwallet.apl.core.transaction.common.TxSerializer#serialize(Transaction, Result)}
         * method to get serialized tx bytes which will by default
         * insert {@link com.apollocurrency.aplwallet.apl.core.app.GenesisImporter#CREATOR_ID} for txs without recipient,
         * but this value is not initialized and always 0, which will lead to incorrect unsignedBytes sequence to sign
         */
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
