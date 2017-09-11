package edu.harvard.hms.dbmi.bd2k.irct.model.script;

public class Where {
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dataType == null) ? 0 : dataType.hashCode());
		result = prime * result + ((src == null) ? 0 : src.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Where other = (Where) obj;
		if (dataType == null) {
			if (other.dataType != null)
				return false;
		} else if (!dataType.equals(other.dataType))
			return false;
		if (src == null) {
			if (other.src != null)
				return false;
		} else if (!src.equals(other.src))
			return false;
		return true;
	}
	public Where(){
		
	}
	
	private String src;
	private String dataType;
	public String getSrc() {
		return src;
	}
	public Where setSrc(String src) {
		this.src = src;
		return this;
	}
	public String getDataType() {
		return dataType;
	}
	public Where setDataType(String dataType) {
		this.dataType = dataType;
		return this;
	}
}
