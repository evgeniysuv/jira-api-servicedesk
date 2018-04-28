package printer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

/**
 * Created by esuv on 4/19/18
 */
public interface PrintService<T> {

    void setHeaderParams(@Nullable Map<String, String> headerParams);

    void setFooterParams(@Nullable Map<String, String> footerParams);

    void printReport(@NotNull Set<T> data)  throws IOException;

    void setReportPath(@NotNull String reportPath);
}
