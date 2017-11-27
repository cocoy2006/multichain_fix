package molab;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.azazar.bitcoin.jsonrpcclient.Bitcoin.Balance;
import com.azazar.bitcoin.jsonrpcclient.BitcoinException;
import com.azazar.bitcoin.jsonrpcclient.BitcoinJSONRPCClient;

import molab.dao.AddressDao;
import molab.entity.Address;
import molab.util.Molab;

@Component
public class StartupRunner implements CommandLineRunner {

	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private Properties properties;
	
	@Autowired
	private AddressDao addressDao;
	
	@Override
	public void run(String... arg0) throws Exception {
		File file = new File(properties.getAddressTxt());
		if(file.exists() && file.isFile()) {
			BitcoinJSONRPCClient client = BitcoinJSONRPCClient.getInstance(properties.getRpcuser(), properties.getRpcpassword(), properties.getRpcurl());
			InputStreamReader read = new InputStreamReader(new FileInputStream(file));
			BufferedReader bufferedReader = new BufferedReader(read);
            String address = null;
            int count = 0;
            while((address = bufferedReader.readLine()) != null){
            	double qty = balanceOfAddress(client, address);
            	Address entity = new Address(address, qty);
            	addressDao.save(entity);
            	if(count++ % 1000 == 0) {
            		log.info(count + " addresses have been handled.");
            	}
            	Thread.sleep(10);
            }
            read.close();
		}
		
	}
	
	private Double balanceOfAddress(BitcoinJSONRPCClient client, String address) {
		List<Balance> list = null;
		try {
			list = client.getAddressBalances(address);
		} catch (BitcoinException e) {
			try {
				client.importAddress(address);
				list = client.getAddressBalances(address);
			} catch (BitcoinException e1) {
				log.error(e1.getMessage());
				log.error("address error: " + address + " not exist!");
			}
		}
		if(list != null && list.size() > 0) {
			Balance b = list.get(0);
			return Molab.rescale(b.qty());
		}
		return 0.0;
	}

}
