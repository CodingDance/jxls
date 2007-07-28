package net.sf.jxls.reader;

import junit.framework.TestCase;
import net.sf.jxls.reader.sample.Department;
import net.sf.jxls.reader.sample.Employee;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.DynaBean;
import org.apache.commons.beanutils.LazyDynaBean;
import org.apache.commons.beanutils.converters.SqlDateConverter;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Leonid Vysochyn
 */
public class XLSBlockReaderTest extends TestCase {
    public static final String dataXLS = "/templates/departmentData.xls";


    protected void setUp() throws Exception {
        super.setUp();
        ConvertUtils.register( new SqlDateConverter(null), java.util.Date.class);
    }

    public void testRead() throws IOException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, InstantiationException {
        InputStream inputXLS = new BufferedInputStream(getClass().getResourceAsStream(dataXLS));
        POIFSFileSystem fsInput = new POIFSFileSystem(inputXLS);
        HSSFWorkbook hssfInputWorkbook = new HSSFWorkbook(fsInput);
        HSSFSheet sheet = hssfInputWorkbook.getSheetAt( 0 );
        List mappings = new ArrayList();
        Department departmentBean = new Department();
        Employee chief = new Employee();
        Map beans = new HashMap();
        beans.put("department", departmentBean);
        beans.put("chief", chief);

        mappings.add( new BeanCellMapping(0, (short) 1, "department.name") );
        mappings.add( new BeanCellMapping("A4", "chief.name"));
        mappings.add( new BeanCellMapping("B4", "chief.age"));
        mappings.add( new BeanCellMapping("C4", "chief.birthDate"));
        mappings.add( new BeanCellMapping("D4", "chief.payment"));
        mappings.add( new BeanCellMapping("E4", "chief.bonus"));

        XLSBlockReader reader = new SimpleBlockReaderImpl(0, 6, mappings);
        XLSRowCursor cursor = new XLSRowCursorImpl( sheet );
        cursor.setSheetName( hssfInputWorkbook.getSheetName(0));
        reader.read( cursor, beans );
        assertEquals( "IT", departmentBean.getName() );
        assertEquals( "Maxim", chief.getName() );
        assertEquals( new Integer(30), chief.getAge() );
        assertEquals( new Double( 3000.0), chief.getPayment() );
        assertEquals( new Double(0.25), chief.getBonus() );
        mappings.clear();
        DynaBean dynaBean = new LazyDynaBean();
        beans.clear();
        beans.put("total", dynaBean);
        reader.setStartRow(8);
        mappings.add( new BeanCellMapping(9, (short) 3, "total", "totalPayment"));
        cursor.setCurrentRowNum( 12 );
        reader.read( cursor, beans );
        assertEquals( new Integer(10100).toString(), dynaBean.get( "totalPayment" ));
    }
}