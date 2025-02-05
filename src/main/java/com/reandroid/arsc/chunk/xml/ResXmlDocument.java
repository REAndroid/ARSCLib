package com.reandroid.arsc.chunk.xml;

import com.reandroid.archive.InputSource;
import com.reandroid.arsc.ApkFile;
import com.reandroid.arsc.chunk.*;
import com.reandroid.arsc.header.HeaderBlock;
import com.reandroid.arsc.io.BlockReader;
import com.reandroid.arsc.pool.ResXmlStringPool;
import com.reandroid.arsc.pool.StringPool;
import com.reandroid.json.JSONObject;
import com.reandroid.utils.collection.CollectionUtil;
import com.reandroid.utils.collection.CombiningIterator;
import com.reandroid.utils.collection.IterableIterator;
import com.reandroid.utils.collection.SingleIterator;
import com.reandroid.utils.io.FileUtil;
import com.reandroid.xml.XMLDocument;
import com.reandroid.xml.XMLFactory;
import com.reandroid.xml.XMLUtil;
import com.reandroid.xml.base.Document;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.*;
import java.util.Iterator;

public class ResXmlDocument extends ResXmlDocumentOrElement implements
        Document<ResXmlElement>, MainChunk, ParentChunk {

    private ApkFile apkFile;

    public ResXmlDocument() {
        super(new ResXmlDocumentChunk());
    }

    @Override
    public ResXmlElement getDocumentElement() {
        ResXmlElement element = CollectionUtil.getFirst(getElements());
        if (element == null) {
            ResXmlDocument childDocument = CollectionUtil
                    .getFirst(iterator(ResXmlDocument.class));
            if (childDocument != null) {
                element = CollectionUtil.getFirst(childDocument.getElements());
            }
            if (element == null) {
                element = newElement();
            }
        }
        return element;
    }
    @Override
    public ApkFile getApkFile() {
        ApkFile apkFile = this.apkFile;
        if (apkFile == null) {
            ResXmlDocument parentDocument = getParentDocument();
            if (parentDocument != null) {
                apkFile = parentDocument.getApkFile();
            }
        }
        return apkFile;
    }
    @Override
    public void setApkFile(ApkFile apkFile) {
        this.apkFile = apkFile;
    }

    @Override
    public TableBlock getTableBlock() {
        return getChunk().getTableBlock();
    }

    @Override
    public StringPool<?> getSpecStringPool() {
        return null;
    }
    @Override
    public MainChunk getMainChunk() {
        return this;
    }
    @Override
    public PackageBlock getPackageBlock() {
        PackageBlock packageBlock = getChunk().getPackageBlock();
        if (packageBlock == null) {
            ResXmlDocument parentDocument = getParentDocument();
            if (parentDocument != null) {
                packageBlock = parentDocument.getPackageBlock();
            }
        }
        return packageBlock;
    }
    public void setPackageBlock(PackageBlock packageBlock) {
        getChunk().setPackageBlock(packageBlock);
    }

    private ResXmlDocument getParentDocument() {
        return getParentInstance(ResXmlDocument.class);
    }

    @Override
    ResXmlDocumentChunk getChunk() {
        return (ResXmlDocumentChunk) super.getChunk();
    }


    @Override
    public XMLDocument toXml(boolean decode) {
        XMLDocument xmlDocument = new XMLDocument();
        xmlDocument.setEncoding(getEncoding());
        Iterator<ResXmlNode> iterator = iterator();
        while (iterator.hasNext()) {
            ResXmlNode node = iterator.next();
            xmlDocument.add(node.toXml(decode));
        }
        return xmlDocument;
    }

    public void readBytes(File file) throws IOException{
        BlockReader reader=new BlockReader(file);
        super.readBytes(reader);
    }
    public void readBytes(InputStream inputStream) throws IOException{
        BlockReader reader=new BlockReader(inputStream);
        super.readBytes(reader);
    }
    public final int writeBytes(File file) throws IOException{
        if(isNull()){
            throw new IOException("Can NOT save null block");
        }
        OutputStream outputStream = FileUtil.outputStream(file);
        int length = super.writeBytes(outputStream);
        outputStream.close();
        return length;
    }
    public String serializeToXml() throws IOException {
        StringWriter writer = new StringWriter();
        XmlSerializer serializer = XMLFactory.newSerializer(writer);
        serialize(serializer);
        serializer.flush();
        writer.flush();
        writer.close();
        return writer.toString();
    }

    @Override
    public boolean isDocument() {
        return true;
    }

    @Override
    String nodeTypeName() {
        return JSON_node_type_document;
    }
    @Override
    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(JSON_encoding, getStringPool().getEncoding());
        jsonObject.put(JSON_node_type, nodeTypeName());
        jsonObject.put(JSON_nodes, nodesToJson());
        return jsonObject;
    }
    @Override
    public void fromJson(JSONObject json) {
        getStringPool().setEncoding(json.optString(JSON_encoding));
        nodesFromJson(json);
        refresh();
    }

    public String getEncoding() {
        if (XMLUtil.KEEP_CHARSET_ENCODING) {
            return getStringPool().getEncoding();
        } else {
            return "utf-8";
        }
    }
    public void setEncoding(String encoding) {
        if (XMLUtil.KEEP_CHARSET_ENCODING) {
            getStringPool().setEncoding(encoding);
        }
    }
    @Override
    public int getLineNumber() {
        return 1;
    }
    @Override
    public void setLineNumber(int lineNumber) {
    }

    @Override
    public int getStartLineNumber() {
        int line = 1;
        ResXmlNode previous = getPrevious();
        if (previous != null) {
            line += previous.getEndLineNumber();
        }
        return line;
    }
    @Override
    public int getEndLineNumber() {
        int line = getStartLineNumber();
        ResXmlNode last = get(size() - 1);
        if (last != null) {
            line += last.getEndLineNumber();
        }
        line = line + 1;
        return line;
    }

    @Override
    ResXmlNodeList getNodeList() {
        return getChunk().getNodeList();
    }
    @Override
    Iterator<ResXmlEvent> getParserEvents() {
        return CombiningIterator.singleTwo(
                ResXmlEvent.startDocument(this),
                new IterableIterator<ResXmlNode, ResXmlEvent>(iterator()) {
                    @Override
                    public Iterator<ResXmlEvent> iterator(ResXmlNode node) {
                        return node.getParserEvents();
                    }
                },
                SingleIterator.of(ResXmlEvent.endDocument(this))
        );
    }

    @Override
    int autoSetLineNumber(int start) {
        int result = 0;
        Iterator<ResXmlNode> iterator = iterator();
        while (iterator.hasNext()) {
            start = iterator.next().autoSetLineNumber(start);
        }
        return result;
    }

    @Override
    public ResXmlStringPool getStringPool() {
        return getChunk().getStringPool();
    }
    public ResXmlIDMap getResXmlIDMap() {
        return getChunk().getResXmlIDMap();
    }

    public void serialize(XmlSerializer serializer) throws IOException {
        serialize(serializer, true);
    }
    @Override
    public void serialize(XmlSerializer serializer, boolean decode) throws IOException {
        PackageBlock packageBlock = getPackageBlock();
        if (decode && packageBlock == null) {
            throw new IOException("Can not decode without package");
        }
        setIndent(serializer, true);
        String encoding = getEncoding();
        serializer.startDocument(encoding, encoding == null ? Boolean.FALSE : null);
        fixNamespaces();
        removeUnusedNamespaces();

        serializeNodes(serializer, decode);

        serializer.endDocument();
    }
    @Override
    public void parse(XmlPullParser parser) throws IOException, XmlPullParserException {
        PackageBlock packageBlock = getPackageBlock();
        if(packageBlock == null){
            throw new IOException("Can not decode without package");
        }
        setPackageBlock(packageBlock);
        removeNullElements();
        int event = parser.getEventType();
        if (event == XmlPullParser.START_DOCUMENT) {
            parser.next();
            setEncoding(parser.getInputEncoding());
        }
        parseInnerNodes(parser);
        refreshFull();
    }

    PackageBlock selectPackageBlock(TableBlock tableBlock) {
        return getChunk().selectPackageBlock(tableBlock);
    }

    public void refreshFull() {
        removeNullElements();
        fixNamespaces();
        removeUnusedNamespaces();
        removeUndefinedAttributes();
        getChunk().refreshFull();
        refresh();
    }


    public static boolean isResXmlBlock(InputSource inputSource) {
        boolean result = false;
        try {
            InputStream inputStream = inputSource.openStream();
            result = isResXmlBlock(inputStream);
            inputStream.close();
        } catch (IOException ignored) {
        }
        return result;
    }
    public static boolean isResXmlBlock(File file) {
        boolean result = false;
        try {
            InputStream inputStream = FileUtil.inputStream(file);
            result = isResXmlBlock(inputStream);
            inputStream.close();
        } catch (IOException ignored) {
        }
        return result;
    }
    public static boolean isResXmlBlock(InputStream inputStream) {
        try {
            HeaderBlock headerBlock = BlockReader.readHeaderBlock(inputStream);
            return isResXmlBlock(headerBlock);
        } catch (IOException ignored) {
            return false;
        }
    }
    public static boolean isResXmlBlock(byte[] bytes){
        try {
            HeaderBlock headerBlock = BlockReader.readHeaderBlock(bytes);
            return isResXmlBlock(headerBlock);
        } catch (IOException ignored) {
            return false;
        }
    }
    public static boolean isResXmlBlock(BlockReader blockReader){
        if(blockReader==null){
            return false;
        }
        try {
            HeaderBlock headerBlock = blockReader.readHeaderBlock();
            return isResXmlBlock(headerBlock);
        } catch (IOException ignored) {
            return false;
        }
    }
    public static boolean isResXmlBlock(HeaderBlock headerBlock){
        if(headerBlock==null){
            return false;
        }
        ChunkType chunkType=headerBlock.getChunkType();
        return chunkType==ChunkType.XML;
    }
}
