package engraving;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A logo from an existing .svg file, kept as vectors (sharpest result).
 *
 * <p>The logo's shapes are copied into the box file, scaled to fit, and every colour is changed
 * to the engrave colour. This works best for single-colour logos. For multi-colour artwork
 * (where white shapes are used to "cut out" parts), export it as a PNG and use
 * {@link ImageEngraving} instead.
 */
public class SvgLogoEngraving implements Engraving {

    private static final Pattern CSS_COLOR = Pattern.compile("(fill|stroke)\\s*:\\s*([^;}\"']+)");

    private final String name;
    private final String content;               // the logo's elements, already recoloured
    private final double minX, minY, width, height;  // the logo's own coordinate box (viewBox)

    /** Loads a logo file. */
    public static SvgLogoEngraving fromFile(Path file) throws IOException {
        if (!Files.isRegularFile(file)) {
            throw new IOException("SVG file not found: " + file);
        }
        return new SvgLogoEngraving(file.getFileName().toString(), Files.readString(file));
    }

    /**
     * @param name shown in the summary (usually the file name)
     * @param svg  the full text of an SVG document
     * @throws IllegalArgumentException if it isn't valid SVG or has no size information
     */
    public SvgLogoEngraving(String name, String svg) {
        this.name = name;
        Element root = parse(svg).getDocumentElement();
        if (!"svg".equals(root.getLocalName())) {
            throw new IllegalArgumentException("Not an SVG file (root element is <" + root.getNodeName() + ">)");
        }

        double[] box = viewBox(root);
        this.minX = box[0];
        this.minY = box[1];
        this.width = box[2];
        this.height = box[3];

        clean(root);
        this.content = serializeChildren(root);
    }

    @Override
    public String toSvg(Bounds area) {
        if (area.isEmpty() || content.isBlank()) {
            return "";
        }
        double scale = Math.min(area.width() / width, area.height() / height);
        double tx = area.centerX() - scale * (minX + width / 2.0);
        double ty = area.centerY() - scale * (minY + height / 2.0);
        return "<g " + EngraveStyle.attributes() + " transform='matrix(" + SvgUtil.num(scale) + " 0 0 "
                + SvgUtil.num(scale) + " " + SvgUtil.num(tx) + " " + SvgUtil.num(ty) + ")'>\n"
                + content + "</g>\n";
    }

    @Override
    public String describe() {
        return "SVG logo " + name;
    }

    // ---------- reading the file ----------

    /** Parses SVG text without ever fetching anything from the internet or the disk (XXE protection). */
    static Document parse(String svg) {
        try {
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            f.setNamespaceAware(true);
            f.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            f.setFeature("http://xml.org/sax/features/external-general-entities", false);
            f.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            f.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            f.setExpandEntityReferences(false);
            DocumentBuilder builder = f.newDocumentBuilder();
            builder.setErrorHandler(new DefaultHandler());   // report errors by exception, don't print them
            return builder.parse(new InputSource(new StringReader(svg)));
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not read SVG: " + e.getMessage(), e);
        }
    }

    /** The logo's coordinate box: its viewBox, or else 0 0 width height. */
    private static double[] viewBox(Element root) {
        String vb = root.getAttribute("viewBox").trim();
        if (!vb.isEmpty()) {
            String[] parts = vb.split("[\\s,]+");
            if (parts.length == 4) {
                double[] box = new double[4];
                for (int i = 0; i < 4; i++) {
                    box[i] = Double.parseDouble(parts[i]);
                }
                if (box[2] > 0 && box[3] > 0) {
                    return box;
                }
            }
        }
        double w = leadingNumber(root.getAttribute("width"));
        double h = leadingNumber(root.getAttribute("height"));
        if (w > 0 && h > 0) {
            return new double[] {0, 0, w, h};
        }
        throw new IllegalArgumentException("SVG has no usable viewBox or width/height, so it can't be scaled");
    }

    private static double leadingNumber(String value) {
        Matcher m = Pattern.compile("^\\s*([0-9]*\\.?[0-9]+)").matcher(value);
        return m.find() ? Double.parseDouble(m.group(1)) : -1;
    }

    // ---------- making it engrave-ready ----------

    /** Removes scripts / event handlers and turns every colour into the engrave colour. */
    private static void clean(Element element) {
        NamedNodeMap attrs = element.getAttributes();
        List<Attr> toRemove = new ArrayList<>();
        for (int i = 0; i < attrs.getLength(); i++) {
            Attr a = (Attr) attrs.item(i);
            String attrName = a.getName().toLowerCase(Locale.ROOT);
            if (attrName.startsWith("on")) {
                toRemove.add(a);
            } else if (attrName.equals("fill") || attrName.equals("stroke")) {
                a.setValue(recolor(a.getValue()));
            } else if (attrName.equals("style")) {
                a.setValue(recolorCss(a.getValue()));
            }
        }
        toRemove.forEach(element::removeAttributeNode);

        NodeList children = element.getChildNodes();
        for (int i = children.getLength() - 1; i >= 0; i--) {
            Node child = children.item(i);
            if (child instanceof Element e) {
                if ("script".equalsIgnoreCase(e.getLocalName())) {
                    element.removeChild(e);
                } else if ("style".equalsIgnoreCase(e.getLocalName())) {
                    e.setTextContent(recolorCss(e.getTextContent()));
                } else {
                    clean(e);
                }
            }
        }
    }

    private static String recolor(String value) {
        return value.trim().equalsIgnoreCase("none") ? value : EngraveStyle.ENGRAVE_COLOR;
    }

    private static String recolorCss(String css) {
        Matcher m = CSS_COLOR.matcher(css);
        StringBuilder out = new StringBuilder();
        while (m.find()) {
            m.appendReplacement(out, Matcher.quoteReplacement(m.group(1) + ":" + recolor(m.group(2))));
        }
        m.appendTail(out);
        return out.toString();
    }

    private static String serializeChildren(Element root) {
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            tf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            Transformer t = tf.newTransformer();
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

            StringBuilder out = new StringBuilder();
            NodeList children = root.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child instanceof Element) {
                    StringWriter w = new StringWriter();
                    t.transform(new DOMSource(child), new StreamResult(w));
                    out.append(w).append('\n');
                }
            }
            return out.toString();
        } catch (Exception e) {
            throw new IllegalArgumentException("Could not copy SVG content: " + e.getMessage(), e);
        }
    }
}
