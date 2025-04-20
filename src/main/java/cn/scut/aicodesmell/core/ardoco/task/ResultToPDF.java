package cn.scut.aicodesmell.core.ardoco.task;

import cn.scut.aicodesmell.exception.CoreTaskException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import edu.kit.kastel.mcse.ardoco.core.api.data.connectiongenerator.InstanceLink;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.Metamodel;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelExtractionState;
import edu.kit.kastel.mcse.ardoco.core.api.data.model.ModelStates;
import edu.kit.kastel.mcse.ardoco.core.api.data.recommendationgenerator.RecommendedInstance;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.ConnectionStateImpl;
import edu.kit.kastel.mcse.ardoco.core.connectiongenerator.ConnectionStatesImpl;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.RecommendationStateImpl;
import edu.kit.kastel.mcse.ardoco.core.recommendationgenerator.RecommendationStatesImpl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

/**
 * @author wanghy
 */
public class ResultToPDF implements ITaskHandler {

    private String downloadPath = System.getProperty("user.dir") + File.separator + "data/files/download/";

    @Override
    public void handle(TaskContext context) {
        RecommendationStatesImpl recommendationStates = context.getRecommendationStates();
        ModelStates modelStatesData = context.getModelStatesData();
        ConnectionStatesImpl connectionStates = context.getConnectionStates();

        String path = downloadPath + context.getProjectId() + ".pdf";
        File finalResult = new File(path);

        //输出pdf
        Document document = new Document();
        try {
            finalResult.createNewFile();
            PdfWriter.getInstance(document, new FileOutputStream(path));
            document.open();

            if (Objects.nonNull(recommendationStates)) {
                document.add(new Paragraph("recommendation states\n"));

                RecommendationStateImpl code = recommendationStates.getRecommendationState(Metamodel.CODE);
                RecommendationStateImpl architecture = recommendationStates.getRecommendationState(Metamodel.ARCHITECTURE);

                document.add(new Paragraph("Code:\n"));
                document.add(new Paragraph("recommended instances:\n"));
                for (RecommendedInstance recommendedInstance : code.getRecommendedInstances()) {
                    document.add(new Paragraph(recommendedInstance.toString()));
                }
                if (!code.getLastAppliedConfiguration().isEmpty()) {
                    document.add(new Paragraph("last applied configuration:\n"));
                    document.add(new Paragraph(code.getLastAppliedConfiguration().toString()));
                }

                document.add(new Paragraph("Architecture:\n"));
                document.add(new Paragraph("recommended instances:\n"));
                for (RecommendedInstance recommendedInstance : architecture.getRecommendedInstances()) {
                    document.add(new Paragraph(recommendedInstance.toString()));
                }
                if (!architecture.getLastAppliedConfiguration().isEmpty()) {
                    document.add(new Paragraph("last applied configuration:\n"));
                    document.add(new Paragraph(code.getLastAppliedConfiguration().toString())); // 注意：此处是否应为 architecture？
                }
            }
            if (Objects.nonNull(modelStatesData)) {
                document.add(new Paragraph("\nmodel states\n"));
                for (String modelId : modelStatesData.modelIds()) {
                    ModelExtractionState modelState = modelStatesData.getModelState(modelId);
                    document.add(new Paragraph("model state {" + modelId + "}\n"));
                    document.add(new Paragraph(modelState.toString() + "\n"));
                }
            }
            if (Objects.nonNull(connectionStates)) {
                document.add(new Paragraph("\nconnection states\n"));

                ConnectionStateImpl code = connectionStates.getConnectionState(Metamodel.CODE);
                ConnectionStateImpl architecture = connectionStates.getConnectionState(Metamodel.ARCHITECTURE);

                document.add(new Paragraph("Code:\n"));
                document.add(new Paragraph("instance links:\n"));
                for (InstanceLink instanceLink : code.getInstanceLinks()) {
                    document.add(new Paragraph(instanceLink.toString() + "\n"));
                }
                if (!code.getLastAppliedConfiguration().isEmpty()) {
                    document.add(new Paragraph("last applied configuration:\n"));
                    document.add(new Paragraph(code.getLastAppliedConfiguration().toString()));
                }

                document.add(new Paragraph("Architecture:\n"));
                document.add(new Paragraph("instance links:\n"));
                for (InstanceLink instanceLink : architecture.getInstanceLinks()) {
                    document.add(new Paragraph(instanceLink.toString() + "\n"));
                }
                if (!architecture.getLastAppliedConfiguration().isEmpty()) {
                    document.add(new Paragraph("last applied configuration:\n"));
                    document.add(new Paragraph(code.getLastAppliedConfiguration().toString())); // 同样：是否应为 architecture？
                }
            }


        } catch (DocumentException | IOException e) {
            throw new CoreTaskException(context.getProjectId());
        } finally {
            document.close();
        }

        context.setFinalResult(finalResult);
    }
}
