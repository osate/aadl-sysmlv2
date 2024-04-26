set -x

FROMDIR=org.osate.contract.repository/target/repository
TODIR=/var/www/html/download/osate/components/aadl2sysml

rm -fr ${TODIR}
mkdir -p ${TODIR}
cp --recursive ${FROMDIR}/* ${TODIR}/.
